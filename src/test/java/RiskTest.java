
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.Application;
import com.mod.loan.common.enums.OrderSourceEnum;
import com.mod.loan.model.DecisionZmDetail;
import com.mod.loan.model.Order;
import com.mod.loan.model.User;
import com.mod.loan.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;


@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class RiskTest {

    @Autowired
    private DecisionPbDetailService decisionPbDetailService;
    @Autowired
    private DecisionZmDetailService zmDetailService;
    @Resource
    private CallBackRongZeService callBackRongZeService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;

    @Test
    public void risk1() throws Exception {
        User user =userService.selectByPrimaryKey((long)1);
        String orderNo="1665673124496871424";
        Order order=orderService.findOrderByOrderNoAndSource(orderNo,OrderSourceEnum.RONGZE.getSoruce());
        DecisionZmDetail zmDetail = zmDetailService.creditApply(user, order);
        System.out.println(JSONObject.toJSONString(zmDetail));
    }

    @Test
    public void risk2() {
        DecisionZmDetail  zmDetail = zmDetailService.selectByOrderNo("1665673124496871424");
        System.out.println(JSONObject.toJSONString(zmDetail));
    }


    @Test
    public void risk3() throws Exception {
        Order order = orderService.findOrderByOrderNoAndSource("1665673124496871424", OrderSourceEnum.RONGZE.getSoruce());
        callBackRongZeService.pushOrderStatus(order);
    }


}
