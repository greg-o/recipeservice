package org.recipeservice

import de.ingogriebsch.spring.hateoas.siren.MediaTypes
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
}

@main
def main(): Unit = SpringApplication.run(classOf[RecipeApp])

