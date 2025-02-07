package com.linkedin.metadata.entity.ebean;

import com.linkedin.common.urn.Urn;
import com.linkedin.data.schema.RecordDataSchema;
import com.linkedin.data.template.RecordTemplate;
import com.linkedin.metadata.entity.RecordTemplateValidator;
import com.linkedin.metadata.utils.PegasusUtils;
import com.linkedin.metadata.dao.utils.RecordUtils;
import com.linkedin.metadata.models.AspectSpec;
import com.linkedin.metadata.models.EntitySpec;
import com.linkedin.metadata.models.registry.EntityRegistry;
import com.linkedin.mxe.SystemMetadata;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

import static com.linkedin.metadata.utils.PegasusUtils.getDataTemplateClassFromSchema;
import static com.linkedin.metadata.entity.EntityService.*;

@Slf4j
public class EbeanUtils {
  private EbeanUtils() {
  }

  @Nonnull
  public static String toJsonAspect(@Nonnull final RecordTemplate aspectRecord) {
    return RecordUtils.toJsonString(aspectRecord);
  }

  @Nonnull
  public static RecordTemplate toAspectRecord(@Nonnull final Urn entityUrn, @Nonnull final String aspectName,
      @Nonnull final String jsonAspect, @Nonnull final EntityRegistry entityRegistry) {
    return toAspectRecord(PegasusUtils.urnToEntityName(entityUrn), aspectName, jsonAspect, entityRegistry);
  }

  /**
   *
   * @param entityName
   * @param aspectName
   * @param jsonAspect
   * @param entityRegistry
   * @return a RecordTemplate which has been validated, validation errors are logged as warnings
   */
  public static RecordTemplate toAspectRecord(@Nonnull final String entityName, @Nonnull final String aspectName,
      @Nonnull final String jsonAspect, @Nonnull final EntityRegistry entityRegistry) {
    final EntitySpec entitySpec = entityRegistry.getEntitySpec(entityName);
    final AspectSpec aspectSpec = entitySpec.getAspectSpec(aspectName);
    final RecordDataSchema aspectSchema = aspectSpec.getPegasusSchema();
    RecordTemplate aspectRecord = RecordUtils.toRecordTemplate(getDataTemplateClassFromSchema(aspectSchema,
            RecordTemplate.class), jsonAspect);
    RecordTemplateValidator.validate(aspectRecord, validationFailure -> {
      log.warn(String.format("Failed to validate record %s against its schema.", aspectRecord));
    });
    return aspectRecord;
  }

  public static SystemMetadata parseSystemMetadata(String jsonSystemMetadata) {
    if (jsonSystemMetadata == null || jsonSystemMetadata.equals("")) {
      SystemMetadata response = new SystemMetadata();
      response.setRunId(DEFAULT_RUN_ID);
      response.setLastObserved(0);
      return response;
    }
    return RecordUtils.toRecordTemplate(SystemMetadata.class, jsonSystemMetadata);
  }
}
