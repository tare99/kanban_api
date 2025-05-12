package com.nsoft.integrations.vibra.kanban_api.api.doc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(name = "LinkDoc", description = "HATEOAS link with href and optional type")
@Getter
public class LinkDoc {
  @Schema(description = "Self URL of the link")
  private LinkType self;

  @Schema(description = "Create link")
  private LinkType create;

  @Schema(description = "Update link")
  private LinkType update;

  @Schema(description = "Patch link")
  private LinkType patch;

  @Schema(description = "Delete link")
  private LinkType delete;

  @Getter
  @Schema(name = "LinkType")
  public static class LinkType {
    @Schema(description = "Target URL of the link", example = "/kanban/api/tasks/1")
    private String href;

    @Schema(description = "Method type", example = "GET")
    private String type;
  }
}
