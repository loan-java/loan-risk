package com.mod.loan.util.zmUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class ZhimiRiskDemo {
    public static void main( String[] args ) {
        for (int i = 0; i < 1; i++){
            String fileContent = null;
            try {
                fileContent = IOUtils.toString(new FileInputStream("sample_data"), StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject sampleObject = JSON.parseObject(String.valueOf(fileContent));
            String model_name = "yiyuan_v1";
            String product = "test";
            String channel = "test";
            String apply_time = sampleObject.getString("apply_time");
            String mobile = sampleObject.getString("mobile");
            String name = sampleObject.getString("name");
            String idcard = sampleObject.getString("idcard");
            String phone_os = "android";
            String user_address = sampleObject.getString("user_address");
            List<EmergencyContact> e_contacts = new ArrayList<EmergencyContact>();
            e_contacts.add(new EmergencyContact(sampleObject.getString("contact1_name"), sampleObject.getString("contact1_phone")));
            e_contacts.add(new EmergencyContact(sampleObject.getString("contact2_name"), sampleObject.getString("contact2_phone")));

            Map<String, String> carrier_data = new HashMap<String, String>();
            JSONObject carrierDataObject = sampleObject.getJSONObject("carrier_data");
            carrier_data.put("mx_report", JSON.toJSONString(carrierDataObject.getJSONObject("mx_report"), SerializerFeature.WriteMapNullValue));
            carrier_data.put("mx_raw", JSON.toJSONString(carrierDataObject.getJSONObject("mx_raw"), SerializerFeature.WriteMapNullValue));

            List<Contact> contact = new ArrayList<Contact>();
            JSONArray contactArray = sampleObject.getJSONArray("contact");
            for (int j = 0; j < contactArray.size(); j++) {
                JSONObject contactItemObject = contactArray.getJSONObject(j);
                contact.add(new Contact(contactItemObject.getString("contact_name"), contactItemObject.getString("contact_phone"), contactItemObject.getString("update_time")));
            }

            ZhimiRiskRequest request = new ZhimiRiskRequest();
            request.setModel_name(model_name);
            request.setProduct(product);
            request.setChannel(channel);
            request.setApply_time(apply_time);
            request.setMobile(mobile);
            request.setName(name);
            request.setIdcard(idcard);
            request.setPhone_os(phone_os);
            request.setUser_address(user_address);
            request.setCarrier_data(carrier_data);
            request.setE_contacts(e_contacts);
            request.setContact(contact);

            String requestStr = JSON.toJSONString(request, SerializerFeature.WriteMapNullValue);

            String url = "http://47.93.185.26/risk/gzip/";
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);
            post.setEntity(new ByteArrayEntity(ZhimiRiskUtil.gzip(requestStr)));
            try {
                HttpResponse response = client.execute(post);
                String responseStr = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                System.out.println(responseStr);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
