package run.halo.twikoo;

import lombok.Data;
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
        // language=html
        return """
                <!-- twikoo start -->
                <script>
                   (() => {
                              const init = () => {
                                  twikoo.init(Object.assign({
                                      el: '#twikoo-wrap',
                                      envId: "%s",
                                      region: '',
                                      onCommentLoaded: function () {
         
                                          $("input").focus(function () {
                                              heo_intype = true;
                                          });
                                          $("textarea").focus(function () {
                                              heo_intype = true;
                                          });
                                          $("input").focusout(function () {
                                              heo_intype = false;
                                          });
                                          $("textarea").focusout(function () {
                                              heo_intype = false;
                                          });
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
                              },
                              const loadTwikoo = () => {
                                  if (typeof twikoo === 'object') {
                                      setTimeout(init, 0)
                                      return
                                  }
                                  getScript("/plugins/PluginTwikoo/assets/static/twikoo.all.min.js").then(init)
                              }
                      
                              if ('Twikoo' === 'Twikoo' || !false) {
                                  if (false) loadComment(document.getElementById('twikoo-wrap'), loadTwikoo)
                                  else loadTwikoo()
                              } else {
                                  window.loadOtherComment = () => {
                                      loadTwikoo()
                                  }
                              }
                          })()
                </script>
                                
                <!-- twikoo end -->
                """.formatted(config.getEnvId());
    }

    @Data
    public static class BasicConfig {
        String envId;
    }
}