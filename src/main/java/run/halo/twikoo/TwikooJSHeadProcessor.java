package run.halo.twikoo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.SettingFetcher;
import run.halo.app.theme.dialect.TemplateHeadProcessor;


@Component
public class TwikooJSHeadProcessor implements TemplateHeadProcessor {

    private final SettingFetcher settingFetcher;

    public TwikooJSHeadProcessor(SettingFetcher settingFetcher) {
        this.settingFetcher = settingFetcher;
    }

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model, IElementModelStructureHandler structureHandler) {
        return settingFetcher.fetch("basic", BasicConfig.class)
            .map(config -> {
                final IModelFactory modelFactory = context.getModelFactory();
                model.add(modelFactory.createText(twikooJsScript(config)));
                return Mono.empty();
            }).orElse(Mono.empty()).then();
    }

    private String twikooJsScript(BasicConfig config) {
        String script = "";

        if (config.getIsPjax()){
            script = script + """
                     <script data-pjax> 
                     (() => {
                    """;
        }else {
            script = script + """
                     <script>
                     (() => {
                    """;
        }

        script = script + """
                              const init = () => {
                                  twikoo.init(Object.assign({
                                      el: '%s',
                                      envId: "%s",
                                      region: '',
                                      onCommentLoaded: function () {
                                           %s
                                      }
                                  }, null))
                              }
                              const loadComment = (dom, callback) => {
                                      if ('IntersectionObserver' in window) {
                                          const observerItem = new IntersectionObserver((entries) => {
                                              if (entries[0].isIntersecting) {
                                                  callback()
                                                  observerItem.disconnect()
                                              }
                                          }, {threshold: [0]})
                                          observerItem.observe(dom)
                                      } else {
                                          callback()
                                      }
                              }                 
                """.formatted(config.getEl(),config.getEnvId(),config.getOnCommentLoaded());


        if (StringUtils.isNotBlank(config.getJs())) {
            // language=html
            script = script + """
                     const loadTwikoo = () => {
                           if (typeof twikoo === 'object') {
                                 setTimeout(init, 0)
                               return
                           }
                           getScript("%s").then(init)
                     }
                    """.formatted(config.getJs());
        }else {
            script = script + """
                     const loadTwikoo = () => {
                           if (typeof twikoo === 'object') {
                                 setTimeout(init, 0)
                               return
                           }
                           getScript("/plugins/PluginTwikoo/assets/static/twikoo.all.min.js").then(init)
                     }
                    """;
        }
            // language=html
        script = script + """
                
                              if ('Twikoo' === 'Twikoo' || !false) {
                                  if (false) loadComment(document.getElementById('%s'), loadTwikoo)
                                  else loadTwikoo()
                              } else {
                                  window.loadOtherComment = () => {
                                      loadTwikoo()
                                  }
                              }
                             
                """.formatted(config.getEl());

        script = script + """  
            })()
                </script>
            """;

        return script;
    }

    @Data
    public static class BasicConfig {
        String envId;
        String el;
        String onCommentLoaded;
        String js;
        Boolean isPjax;


    }
}