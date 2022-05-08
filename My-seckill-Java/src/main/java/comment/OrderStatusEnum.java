package comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    NO_PAY(0),  // 未支付
    IS_PAY(1),  // 已支付
    IS_SHIP(2), // 已发货
    IS_RECEIVE(3), // 已收货
    IS_RETURN(4),  // 已退货
    IS_COMPLETE(5); // 已完成
    private final Integer status;
}
