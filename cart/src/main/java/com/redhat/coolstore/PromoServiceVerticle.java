package com.redhat.coolstore;

import com.redhat.coolstore.model.Promotion;
import com.redhat.coolstore.model.ShoppingCart;
import com.redhat.coolstore.model.impl.FreeShippingPromotion;
import com.redhat.coolstore.model.impl.ProductCombinationPromotion;
import com.redhat.coolstore.model.impl.SingleProductPromotion;
import com.redhat.coolstore.utils.Transformers;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.math3.util.Precision;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class PromoServiceVerticle extends AbstractVerticle{

    private final Logger logger = LoggerFactory.getLogger(PromoServiceVerticle.class.getName());

    private static final List<Promotion> promos = Arrays.asList(new FreeShippingPromotion(200), //Free shipping if above 200 dollar
        new SingleProductPromotion(30, "444435"), //Thirty dollar discount on Oculus Rift
        new ProductCombinationPromotion(Arrays.asList("329299", "329199"), 10) //Ten dollar discount on combination of Red Hat Fedora and Stickers
    );


    @Override
    public void start() {
        logger.info("Starting " + this.getClass().getSimpleName());
        EventBus eb = vertx.eventBus();

        MessageConsumer<String> consumer = eb.consumer("promo");
        consumer.handler(message -> {
            //The body of the message should be the shopping cart as Json.
            ShoppingCart cart = Transformers.jsonToShoppingCart(new JsonObject(message.body()));
            double sumPromos = promos.stream().mapToDouble(p -> p.isCriteriaMet(cart) ? Math.abs(p.getPromotion()) : 0).sum();
            message.reply(new JsonObject().put("promoValue", Precision.round(sumPromos, 2, BigDecimal.ROUND_HALF_UP)).encode());

        });
    }
}
