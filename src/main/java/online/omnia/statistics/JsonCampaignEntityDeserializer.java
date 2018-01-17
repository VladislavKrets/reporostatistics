package online.omnia.statistics;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by lollipop on 27.12.2017.
 */
public class JsonCampaignEntityDeserializer implements JsonDeserializer<JsonCampaignEntity>{
    @Override
    public JsonCampaignEntity deserialize(JsonElement jsonElement, Type type,
                                          JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        JsonCampaignEntity campaignEntity = new JsonCampaignEntity();
        String url = null;
        JsonElement temp = object.get("banners");
        if (temp != null) {
            JsonArray array = temp.getAsJsonArray();
            for (JsonElement element : array) {
                url = element.getAsJsonObject().get("banner_url").getAsString();
                break;
            }
        }
        else url = "";
        campaignEntity.setUrl(url);
        campaignEntity.setId(object.get("campaign").getAsInt());
        campaignEntity.setName(object.get("campaign_name").getAsString());
        return campaignEntity;
    }
}
