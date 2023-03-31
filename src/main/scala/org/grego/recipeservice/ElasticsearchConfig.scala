// Recipe Service
// Created by: Greg Osgood
// Copyright: none

package org.grego.recipeservice

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.{ElasticsearchClients, ElasticsearchTemplate}
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import org.springframework.http.HttpHeaders

@Configuration
@EnableElasticsearchRepositories(basePackages = Array("org.grego.springboot.recipeservice.repository"))
class ElasticsearchConfig {
  private val ELASTICSEARCH_HEADERS = "application/vnd.elasticsearch+json;compatible-with=7"
  @Value("${spring.data.elasticsearch.client.endpoints}")
  private var endpoints: String = _
  @Value("${spring.data.elasticsearch.client.username}")
  private var username: String = _
  @Value("${spring.data.elasticsearch.client.password}")
  private var password: String = _

  @Bean
  @throws[Exception]
  def client: ElasticsearchClient = {
    val compatibilityHeaders = new HttpHeaders
    compatibilityHeaders.add(javax.ws.rs.core.HttpHeaders.ACCEPT, ELASTICSEARCH_HEADERS)
    compatibilityHeaders.add(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, ELASTICSEARCH_HEADERS)
    val clientConfiguration = ClientConfiguration.builder
      .connectedTo(endpoints)
      .usingSsl
      .withBasicAuth(username, password)
      .withDefaultHeaders(compatibilityHeaders)
      .build
    ElasticsearchClients.createImperative(clientConfiguration)
  }

  @Bean(Array("elasticsearchTemplate"))
  @throws[Exception]
  def elasticsearchTemplate: ElasticsearchTemplate = new ElasticsearchTemplate(client, new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext))
}
