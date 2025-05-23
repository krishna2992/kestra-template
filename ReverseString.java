package io.kestra.plugin.templates;

// import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.property.Property;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import org.slf4j.Logger;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Reverse a string",
    description = "Reverse all letters from a string"
)
public class ReverseString extends Task implements RunnableTask<ReverseString.Output> {
    @Schema(
        title = "The base string you want to reverse"
    )
    private Property<String> arg1;

    @Schema(
        title = "The base string you want to reverse"
    )
    private Property<String> arg2;

    @Override
    public ReverseString.Output run(RunContext runContext) throws Exception {
        Logger logger = runContext.logger();

        String render1 = runContext.render(arg1).as(String.class).orElse(null);
        String render2 = runContext.render(arg2).as(String.class).orElse(null);
        logger.debug("arg1:"+render1);
        logger.debug("arg2:"+render2);

        return Output.builder()
            .reversed(StringUtils.reverse(render1))
            .reversed2(StringUtils.reverse(render2))
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "The reversed string "
        )
        private final String reversed;

        @Schema(
            title = "Same string to check "
        )
        private final String reversed2;
    }

    
}