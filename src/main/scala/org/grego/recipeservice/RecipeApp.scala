// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice

import de.ingogriebsch.spring.hateoas.siren.MediaTypes
import io.swagger.v3.oas.models.info.{Info, License}
import io.swagger.v3.oas.models.{Components, OpenAPI}
import org.eclipse.jetty.server.{NetworkTrafficServerConnector, Server}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.{EnableAutoConfiguration, SpringBootApplication}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Profile}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@ComponentScan
@Configuration
@EnableJpaRepositories
@EnableAutoConfiguration
@EnableTransactionManagement
class RecipeApp {
  @Bean
  def customOpenAPI(@Value("${springdoc.version:3.0.0}") appVersion: String): OpenAPI = {
    new OpenAPI()
      .components(new Components())
      .info(new Info()
        .title("Recipe Service")
        .version(appVersion)
        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
  }
}

object RecipeApp {
  @main
  def main(): Unit = {
    SpringApplication.run(classOf[RecipeApp])
  }
}

