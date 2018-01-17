package online.omnia.statistics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by lollipop on 27.12.2017.
 */
public class Main {
    public static int days;
    public static long deltaTime = 24L * 60 * 60 * 1000;

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException, InterruptedException {
        if (args.length != 1) return;
        if (!args[0].matches("\\d+")) return;
        if (Integer.parseInt(args[0]) == 0) {
            days = 0;
            deltaTime = 0;
        }
        days = Integer.parseInt(args[0]);

        List<AccountsEntity> accountsEntities = MySQLDaoImpl.getInstance().getAccountsEntities("reporo");
        String token = "016LYLW9XgfmPBNt3O1iMdZ336AFwp20";
        String secret = "r02h4v4l9C97BEEJdV8xGSCTxyFpe20YetnL55e5fdOD71TM1Odz9TgC0CvY47rk";
        String seconds = String.valueOf((System.currentTimeMillis() / 1000));
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        GsonBuilder groupBuilder = new GsonBuilder();
        groupBuilder.registerTypeAdapter(List.class, new JsonCampaignGroupListDeserializer());
        Gson gsonGroup = groupBuilder.create();
        GsonBuilder campaignBuilder = new GsonBuilder();
        campaignBuilder.registerTypeAdapter(List.class, new JsonCampaignListDeserializer());
        campaignBuilder.registerTypeAdapter(JsonCampaignEntity.class, new JsonCampaignEntityDeserializer());
        GsonBuilder statisticsBuilder = new GsonBuilder();
        statisticsBuilder.registerTypeAdapter(List.class, new JsonStatisticsDeserializer());
        Gson gsonStatistics = statisticsBuilder.create();
        Gson gsonCampaign = campaignBuilder.create();
        byte[] hash;
        StringBuilder h = new StringBuilder();
        String answer;
        List<Integer> campaignGroups;
        List<Integer> campaigns;
        JsonCampaignEntity jsonCampaignEntity;
        List<SourceStatisticsEntity> sourceStatisticsEntities;
        Map<String, String> parameters = null;
        SourceStatisticsEntity entity;
        for (AccountsEntity accountsEntity : accountsEntities) {
            token = accountsEntity.getApiKey();
            secret = accountsEntity.getClientSecret();
            hash = digest.digest((seconds + secret).getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) h.append('0');
                h.append(hex);
            }
            System.out.println(h.toString());
            answer = HttpMethodUtils.getMethod("http://api.reporo.com/analytics/data-api.php?action="
                            + URLEncoder.encode("inventory/advertiser", "UTF-8"),
                    token, seconds, h.toString());
            System.out.println(answer);
            campaignGroups = gsonGroup.fromJson(answer, List.class);
            for (Integer group : campaignGroups) {
                Thread.sleep(5000);
                answer = HttpMethodUtils.getMethod("http://api.reporo.com/analytics/data-api.php?action="
                                + URLEncoder.encode("inventory/advertiser/campaign_group/" + group, "UTF-8"),
                        token, seconds, h.toString());
                System.out.println(answer);
                campaigns = gsonCampaign.fromJson(answer, List.class);
                for (Integer campaignId : campaigns) {
                    Thread.sleep(5000);
                    answer = HttpMethodUtils.getMethod("http://api.reporo.com/analytics/data-api.php?action="
                                    + URLEncoder.encode("inventory/advertiser/campaign/" + campaignId, "UTF-8"),
                            token, seconds, h.toString());
                    System.out.println(answer);
                    jsonCampaignEntity = gsonCampaign.fromJson(answer, JsonCampaignEntity.class);
                    System.out.println(jsonCampaignEntity);
                    for (int i = 0; i <= days; i++) {
                        Thread.sleep(5000);
                        answer = HttpMethodUtils.getMethod("http://api.reporo.com/analytics/data-api.php?action="
                                        + URLEncoder.encode("statistics/advertiser/campaign/" + campaignId, "UTF-8")
                                        + "&from="
                                        + simpleDateFormat.format(new Date(System.currentTimeMillis() - i * 24L * 60 * 60 * 1000 - deltaTime))
                                        + "&to="
                                        + simpleDateFormat.format(new Date(System.currentTimeMillis() - i * 24L * 60 * 60 * 1000 - deltaTime)),
                                token, seconds, h.toString());
                        System.out.println(answer);

                        sourceStatisticsEntities = gsonStatistics.fromJson(answer, List.class);
                        for (SourceStatisticsEntity statisticsEntity : sourceStatisticsEntities) {
                            statisticsEntity.setCampaignName(jsonCampaignEntity.getName());
                            
                            statisticsEntity.setCampaignId(String.valueOf(jsonCampaignEntity.getId()));
                            statisticsEntity.setReceiver("API");
                            statisticsEntity.setAccount_id(accountsEntity.getAccountId());
                            statisticsEntity.setBuyerId(accountsEntity.getBuyerId());
                            
                            statisticsEntity.setDate(new java.sql.Date(System.currentTimeMillis() - i * 24L * 60 * 60 * 1000 - deltaTime));
                            if (jsonCampaignEntity.getUrl() != null) {
                                parameters = Utils.getUrlParameters(jsonCampaignEntity.getUrl());
                                if (parameters.containsKey("cab")) {
                                    if (parameters.get("cab").matches("\\d+")
                                            && MySQLDaoImpl.getInstance().getAffiliateByAfid(Integer.parseInt(parameters.get("cab"))) != null) {
                                        statisticsEntity.setAfid(Integer.parseInt(parameters.get("cab")));
                                    } else {
                                        statisticsEntity.setAfid(0);
                                    }
                                } else statisticsEntity.setAfid(2);
                            }
                            System.out.println(parameters);
                            if (Main.days != 0) {
                                entity = MySQLDaoImpl.getInstance()
                                        .getSourceStatistics(statisticsEntity.getAccount_id(),
                                                statisticsEntity.getCampaignId(),
                                                statisticsEntity.getDate());
                                if (entity == null) {
                                    MySQLDaoImpl.getInstance().addSourceStatistics(statisticsEntity);
                                    System.out.println(statisticsEntity);
                                } else {
                                    statisticsEntity.setId(entity.getId());
                                    MySQLDaoImpl.getInstance().updateSourceStatistics(statisticsEntity);
                                    entity = null;
                                }
                            }
                            else {
                                if (MySQLDaoImpl.getInstance().isDateInTodayAdsets(statisticsEntity.getDate(), statisticsEntity.getAccount_id(), statisticsEntity.getCampaignId())) {
                                    MySQLDaoImpl.getInstance().updateTodayAdset(Utils.getAdset(statisticsEntity));
                                } else MySQLDaoImpl.getInstance().addTodayAdset(Utils.getAdset(statisticsEntity));

                            }

                        }
                    }
                }
            }
        }
        MySQLDaoImpl.getSessionFactory().close();
    }
}
