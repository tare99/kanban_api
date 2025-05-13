package com.nsoft.integrations.vibra.kanban_api.api.doc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nsoft.integrations.vibra.kanban_api.api.doc.LinkDoc.LinkType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import org.springframework.hateoas.PagedModel.PageMetadata;

@Schema(name = "PagedDoc")
@Getter
public class PagedDoc {

  @JsonProperty("_embedded")
  @Schema
  private EmbeddedDoc _embedded;

  @Schema(description = "Resource links")
  @JsonProperty("_links")
  private LinkPagedDoc _links;

  @Schema(description = "Pages")
  private PageMetadata page;

  @Getter
  @Schema(name = "EmbeddedDoc")
  public static class EmbeddedDoc {
    @Schema private List<TaskDoc> tasks;
  }

  @Getter
  @Schema(name = "LinkPagedDoc")
  public static class LinkPagedDoc {
    @Schema(description = "Self URL of the link")
    private LinkType self;

    @Schema(description = "Create link")
    private LinkType create;
  }
}
