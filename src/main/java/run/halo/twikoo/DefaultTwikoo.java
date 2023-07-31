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

/**
 * prismjs 插件
 *
 * @author liuzhihang
 * @date 2022/10/23
 */
@Component
public class DefaultTwikoo implements TemplateHeadProcessor {

    private final SettingFetcher settingFetcher;

    public DefaultTwikoo(SettingFetcher settingFetcher) {
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
                      
                              const getCount = () => {
                                  twikoo.getCommentsCount({
                                      envId: "%s",
                                      region: '',
                                      urls: [window.location.pathname],
                                      includeReply: true
                                  }).then(function (res) {
                                      document.getElementById('twikoo-count').innerText = res[0].count
                                  }).catch(function (err) {
                                  });
                              }
                      
                              const runFn = () => {
                                  init()
                                  true && getCount()
                              }
                      
                              const loadTwikoo = () => {
                                  if (typeof twikoo === 'object') {
                                      setTimeout(runFn, 0)
                                      return
                                  }
                                  getScript("/plugins/PluginTwikoo/assets/static/twikoo.all.min.js").then(runFn)
                              }
                      
                              if ('Twikoo' === 'Twikoo' || !false) {
                                  if (false) btf.loadComment(document.getElementById('twikoo-wrap'), loadTwikoo)
                                  else loadTwikoo()
                              } else {
                                  window.loadOtherComment = () => {
                                      loadTwikoo()
                                  }
                              }
                      
                      
                          })()
                </script>
                                
                <!-- twikoo end -->
                """.formatted(config.getEnvId(),config.getEnvId());
    }

    @Data
    public static class BasicConfig {
        String envId;
    }
}