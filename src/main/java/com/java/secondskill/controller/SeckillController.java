package com.java.secondskill.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.java.secondskill.beans.SeckillOrder;
import com.java.secondskill.beans.User;
import com.java.secondskill.rabbitmq.MqSender;
import com.java.secondskill.rabbitmq.SeckillMessage;
import com.java.secondskill.redis.GoodsKey;
import com.java.secondskill.result.CodeMsg;
import com.java.secondskill.result.Result;
import com.java.secondskill.servers.GoodsService;
import com.java.secondskill.servers.OrderService;
import com.java.secondskill.servers.RedisService;
import com.java.secondskill.servers.SeckillService;
import com.java.secondskill.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private MqSender mqSender;


    //做标记，判断该商品是否被处理过了
    private HashMap<Long, Boolean> localOverMap = new HashMap<>();

    //基于令牌桶算法的限流实现类
    private static RateLimiter rateLimiter;

    static {
        rateLimiter = RateLimiter.create(10);
    }

    /**
     * 系统初始化,将商品信息加载到redis和本地内存
     */
    @Override
    public void afterPropertiesSet() {
        List<GoodsVo> goodsVos = goodsService.listGoodsVo();
        if (CollectionUtils.isEmpty(goodsVos)) {
            return;
        }
        for (GoodsVo goodsVo : goodsVos) {
            redisService.set(GoodsKey.getGoodsStock, "" + goodsVo.getId(), goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);
        }
    }

    /**
     * GET POST
     * 1、GET幂等,服务端获取数据，无论调用多少次结果都一样
     * 2、POST，向服务端提交数据，不是幂等
     * 将同步下单改为异步下单
     */
    @RequestMapping(value = "/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model, User user, @RequestParam("goodsId") long goodsId) {
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MICROSECONDS)) {
            return Result.error(CodeMsg.ACCESS_LIMIT_REACHED);
        }
        if (Objects.isNull(user)) {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
        Boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.SECKILL_OVER);
        }
        //预减库存
        Long stock = redisService.decr(GoodsKey.getGoodsStock, "" + goodsId);
        if (stock < 0) {
//            afterPropertiesSet();
//            stock = redisService.decr(GoodsKey.getGoodsStock, "" + goodsId);
//            if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SECKILL_OVER);
//            }
        }
        //判断重复秒杀
        SeckillOrder order = orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
        if (Objects.nonNull(order)) {
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
        //将秒杀请求放入rabbitMQ队列中
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setGoodsId(goodsId);
        seckillMessage.setUser(user);
        mqSender.sendSeckillMessage(seckillMessage);
        return Result.success(0);//排队中
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, User user, @RequestParam("goodsId") Long goodsId) {
        model.addAttribute("user", user);
        if (Objects.isNull(user)) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long seckillResult = seckillService.getSeckillResult(user.getId(), goodsId);
        return Result.success(seckillResult);
    }
}
