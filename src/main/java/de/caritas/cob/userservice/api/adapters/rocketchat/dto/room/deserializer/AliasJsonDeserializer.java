package de.caritas.cob.userservice.api.adapters.rocketchat.dto.room.deserializer;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.caritas.cob.userservice.api.adapters.web.dto.AliasMessageDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.VideoCallMessageDTO;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

/** Json Deserializer for the alias. */
public class AliasJsonDeserializer extends JsonDeserializer<AliasMessageDTO> {

  private final UsernameTranscoder usernameTranscoder = new UsernameTranscoder();
  private final AliasMessageConverter aliasMessageConverter = new AliasMessageConverter();

  /**
   * Deserializes the Rocket.Chat custom alias object. The whole new {@link AliasMessageDTO}
   * containing a {@link VideoCallMessageDTO} will be transformed.
   *
   * @param jsonParser the json parser object containing the source object as a string
   * @param context the current context
   * @return the generated/deserialized {@link AliasMessageDTO}
   */
  @Override
  public AliasMessageDTO deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException {

    String aliasValue = jsonParser.getValueAsString();
    if (StringUtils.isBlank(aliasValue)) {
      return null;
    }

    return buildAliasMessageDTOWithPossibleVideoCallMessageDTO(aliasValue);
  }

  private AliasMessageDTO buildAliasMessageDTOWithPossibleVideoCallMessageDTO(String aliasValue) {
    AliasMessageDTO alias =
        aliasMessageConverter.convertStringToAliasMessageDTO(aliasValue).orElse(null);
    if (nonNull(alias)) {
      decodeUsernameOfVideoCallMessageDTOIfNonNull(alias);
    }
    return alias;
  }

  private void decodeUsernameOfVideoCallMessageDTOIfNonNull(AliasMessageDTO alias) {
    if (nonNull(alias.getVideoCallMessageDTO())) {
      alias
          .getVideoCallMessageDTO()
          .setInitiatorUserName(
              usernameTranscoder.decodeUsername(
                  alias.getVideoCallMessageDTO().getInitiatorUserName()));
    }
  }
}
