
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.Application;
import com.mod.loan.model.DecisionZmDetail;
import com.mod.loan.model.User;
import com.mod.loan.service.DecisionPbDetailService;
import com.mod.loan.service.DecisionZmDetailService;
import com.mod.loan.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class RiskTest {

    @Autowired
    private DecisionPbDetailService decisionPbDetailService;
    @Autowired
    private DecisionZmDetailService zmDetailService;


    @Autowired
    private UserService userService;

    @Test
    public void risk1() throws Exception {
        User user =userService.selectByPrimaryKey((long)1);
        String orderNo="1665673124496871424";
        DecisionZmDetail zmDetail = zmDetailService.creditApply(user, orderNo);
        System.out.println(JSONObject.toJSONString(zmDetail));
    }

    @Test
    public void risk2() {
        DecisionZmDetail  zmDetail = zmDetailService.selectByOrderNo("1665673124496871424");
        System.out.println(JSONObject.toJSONString(zmDetail));
    }


}
