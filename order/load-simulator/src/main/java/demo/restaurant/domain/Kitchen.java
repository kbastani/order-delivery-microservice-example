package demo.restaurant.domain;

import scheduler.Cart;
import scheduler.ExpandingResource;
import scheduler.Track;

public class Kitchen extends Track<OrderDelivery> {
    public Kitchen() {
        super(new ExpandingResource<Cart<OrderDelivery>, OrderDelivery>(new Cart[]{}, Cart::new,
                new KitchenRepository()));
    }
}
