package com.java.secondskill.servers;

import com.java.secondskill.beans.SeckillGoods;
import com.java.secondskill.constants.Common;
import com.java.secondskill.mapper.GoodsMapper;
import com.java.secondskill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 查询所有商品
     */
    public List<GoodsVo> listGoodsVo() {
        return goodsMapper.listGoodsVo();
    }

    /**
     * 根据id查询指定商品
     */
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsMapper.getGoodsVoByGoodsId(goodsId);
    }

    /**
     * 减少库存，每次减1
     * 乐观锁，最大重试次数5次
     */
    public boolean reduceStock(GoodsVo goodsVo) {
        AtomicInteger numAttempts = new AtomicInteger(0);
        int ret;
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setGoodsId(goodsVo.getId());
        seckillGoods.setVersion(goodsVo.getVersion());
        do {
            numAttempts.incrementAndGet();
            int version = goodsMapper.getVersionByGoodsId(goodsVo.getId());
            seckillGoods.setVersion(version);
            ret = goodsMapper.reduceStockByVersion(seckillGoods);
            if (ret != 0) {
                break;
            }
        } while (numAttempts.get() < Common.DEFAULT_MAX_RETRIES);
        return ret > 0;
    }
}
