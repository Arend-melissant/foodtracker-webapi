package com.foodtracker.infrastructure.adapter.inbound.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/swagger")
public class SwaggerUiResource {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getSwaggerUi() {
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>FoodTracker WebAPI - Swagger UI</title>
                  <link rel="stylesheet" type="text/css" href="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui.css" />
                  <style>
                    html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
                    *, *:before, *:after { box-sizing: inherit; }
                    body { margin:0; background: #fafafa; }
                    .topbar { background-color: #1b5e20 !important; }
                  </style>
                </head>
                <body>
                  <div id="swagger-ui"></div>
                  <script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-bundle.js"></script>
                  <script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-standalone-preset.js"></script>
                  <script>
                  window.onload = function() {
                    const ui = SwaggerUIBundle({
                      url: "/openapi.json",
                      dom_id: '#swagger-ui',
                      deepLinking: true,
                      presets: [
                        SwaggerUIBundle.presets.apis,
                        SwaggerUIStandalonePreset
                      ],
                      plugins: [
                        SwaggerUIBundle.plugins.DownloadUrl
                      ],
                      layout: "StandaloneLayout"
                    });
                    window.ui = ui;
                  };
                  </script>
                </body>
                </html>
                """;
        return Response.ok(html).build();
    }
}

