package io.diligencevault.plugin.core.tasks;

import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.property.Property;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import org.slf4j.Logger;


import java.io.InputStream;
import java.net.URI;

import java.net.http.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Update Custom Fields",
    description = "Use this task to uddate Custom fields using DV API"
)
@Plugin(
    examples = {
        @Example(
            full = true,
            title = "Update Custom Fields for Project, Firm",
            code = """
                id: myflow.sample.plugin
                namespace: company.team

                tasks:
                    - id: def_get_access_token
                      type: io.kestra.plugin.core.http.Request
                      uri: "https://dv-feature-api.diligencevault.com/api/auth/token" 
                      method: POST
                      formData:
                        client_id: DvApp
                        firm_id: 68292
                        grant_type: password
                        username: krishna.suryawanshi@diligencevault.com
                        secret_key: f201b579-9def-4e5d-9b55-8fd15a287a41
                      options:
                        followRedirects: true
                        ssl:
                          insecureTrustAllCertificates: true
                        allowFailed: true

                    - id: custom_field_update
                      type: io.diligencevault.plugin.core.tasks.CustomFieldUpdate
                      apihost: https://dv-feature-api.diligencevault.com
                      allowInsecure: true
                      entityId: 158359
                     fields:
                        age: 18585
                      token: "{{ outputs.def_get_access_token.body| jq('.access_token')| first }}"
                      schemaType: duediligence"""
        )
    }
)
public class CustomFieldUpdate extends Task implements RunnableTask<CustomFieldUpdate.Output> {
    @Schema(
        title = "DV API HOST",
        description = "This will be used as hostname for dv endpoints"
    )
    @NotNull
    private Property<String> apihost;
    @Schema(
        title = "Short description for this input",
        description = "Full description of this input"
    )

    private Property<String> format;

    @Schema(
        title = "Access Token",
        description = "Access token to make API request to DV endpoint"
    )
    @NotNull
    private Property<String> token;

    @Schema(
        title = "Fields to Update",
        description = "Provide a map of (key, value) pair of fields to update.Only those fields will updated that are provided"
    )
    @NotNull
    private Property<Map<String, Object>> fields;

    @Schema(
        title = "Allow Insecure Https requests",
        description = "This option can be used to make API requests to self signed https servers.Defaults to false."
    )
    @Builder.Default
    private Property<Boolean> allowInsecure = Property.of(false);

    @Schema(
        title = "Schematype of entity",
        description = "This is used in making request to DV API. Eg. duediligence, firm"
    )
    private Property<String> schemaType;

    @Schema(
        title = "ID for Custom fields parent",
        description = "Entity ID for the type of custom fields being updated i.e. for firm it will be firm ID,etc"
    )
    private Property<Integer> entityId;

    @Builder.Default
    @Getter(AccessLevel.NONE)
    final Map<String, Integer> schemaMap = Map.of(
        "fund", 1219,
        "firm", 1220,
        "vehicle", 1217,
        "strategy", 5004,
        "duediligence", 1105,
        "contact", 1218
    );

    @Override
    public CustomFieldUpdate.Output run(RunContext runContext) throws Exception {
        try{

                Logger logger = runContext.logger();
                ObjectMapper mapper = new ObjectMapper();
                String renderedApiHost = runContext.render(apihost).as(String.class).orElse("");
                Integer renderedEntityId = runContext.render(entityId).as(Integer.class).orElse(0);
                String renderedJwtToken = runContext.render(token).as(String.class).orElse("");
                String renderedSchemaType = runContext.render(schemaType).as(String.class).orElse("");
                Map<String, Object> renderedFields = runContext.render(fields).asMap(String.class, Object.class);
                boolean renderedInsecure = runContext.render(allowInsecure).as(Boolean.class).orElse(false); 
                String url = renderedApiHost.endsWith("/") ? renderedApiHost + "api/service/dvapi_service/get_custom_fields_data" : renderedApiHost + "/api/service/dvapi_service/get_custom_fields_data";
                int entity_type= schemaMap.get(renderedSchemaType);

                Map<String, Object> requestData = Map.of(
                    "entity_id", renderedEntityId,
                    "entity_type", entity_type,
                    "schema_type", renderedSchemaType,
                    "sub_entity_id", 0
                );
                String reqBody = mapper.writeValueAsString(requestData);

                HttpClient client = new Httpclient(renderedInsecure).getClient(); 
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("page-url", "/app/home")
                    .header("Accept-Encoding", "gzip")
                    .header("Authorization", "Bearer " + renderedJwtToken)
                    .POST(HttpRequest.BodyPublishers.ofString(reqBody))
                    .build();

    
                ResponceDecoder decoder = new ResponceDecoder();
                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                String responceString = decoder.GZIPToString(response.body());
                Map<String, Object> responseJson = (Map<String,Object>)mapper.readValue(responceString, Map.class);
                Map<String, Object> updatedSchema = new SchemaUpdater().updateSchema(responseJson, renderedFields);
                

                String updateBody = mapper.writeValueAsString(updatedSchema);
                String uploadUrl = renderedApiHost.endsWith("/") ? renderedApiHost + "api/service/dvapi_service/post_custom_fields_data" : renderedApiHost + "/api/service/dvapi_service/post_custom_fields_data";

                HttpRequest updateRequest = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", "application/json")
                    .header("page-url", "/app/home")
                    .header("Accept-Encoding", "gzip")
                    .header("Authorization", "Bearer " + renderedJwtToken)
                    .POST(HttpRequest.BodyPublishers.ofString(updateBody))
                    .build();

                HttpResponse<InputStream> postresponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofInputStream());
                HttpHeaders postHeaders = postresponse.headers();

                Optional<String> contentEncoding = postHeaders.firstValue("Content-Encoding");
                String postresponceString;
                if (contentEncoding.isPresent() && contentEncoding.get().equalsIgnoreCase("gzip")){
                    postresponceString = decoder.GZIPToString(postresponse.body());
                } else {
                    postresponceString = decoder.streamToString(postresponse.body());   
                }

                Object body = JsonDecoder.parseJsonFlexible(postresponceString);
                
                    
                //  Build and Return Output
                return Output.builder()
                    .endpoint("/pi/service/dvapi_service/post_custom_fields_data")
                    .status(postresponse.statusCode())
                    .headers(postHeaders.map())
                    .body(body)
                    .build();
                
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Responce body as JSON"
            // description = "Full description of this output"
        )
        private String endpoint;
        @Schema(
            title = "Responce body as JSON"
            // description = "Full description of this output"
        )
        private Object body;
        @Schema(
            title = "Response body"
            // description = "Full description of this output"
        )
        private String res;
        @Schema(
            title = "Responce Headers"
            // description = "Full description of this output"
        )
        private Map<String, List<String>> headers;
        @Schema(
            title = "Responce Status"
            // description = "Full description of this output"
        )
        private int status;
    }
}
