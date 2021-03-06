package org.openrepose.gradle.plugins.jaxb.resolver

import spock.lang.Unroll

import org.openrepose.gradle.plugins.jaxb.converter.NamespaceToEpisodeConverter
import org.openrepose.gradle.plugins.jaxb.fixtures.TreeFixture

class EpisodeDependencyResolverSpec extends TreeFixture {
  
  def resolver = new EpisodeDependencyResolver()

  def episodesDir = new File("/build/generated-resources/episodes")
  def externalNamespaces = ["E1", "E2", "E3", "E4"]
  def converter = Mock(NamespaceToEpisodeConverter)

  def setup() {
    def namespaces = xsdNamespaces + externalNamespaces
    namespaces.each { namespace ->
      with(converter) {
	convert(namespace) >> namespace + ".episode"
      }
    }
    ["xsd6":["E3"], "xsd3":["E1"], "xsd1":["E1"], "xsd2":["E2"],
     "xsd9":["E4"]].each { ns, external ->
       nodes.find { it.data.namespace == ns }.data.externalDependencies = external as Set
    }
  }

  @Unroll
  def "resolve '#namespace' for '#episodeDependencyNames.size()' dependencies" () {
    given:
    def node = nodes.find { it.data.namespace == namespace }

    when:
    def result = resolver.resolve(node, converter, episodesDir)

    then:
    result.size() == episodeDependencyNames.size()
    result.name.containsAll(episodeDependencyNames.collect { it + ".episode"})
    
    where:
    namespace || episodeDependencyNames
    "xsd6"    || ["E3", "E1", "xsd3", "xsd1"]
    "xsd8"    || ["E2", "xsd4", "xsd5", "E1", "xsd1", "xsd2"]
    "xsd9"    || ["E2", "E4", "xsd5", "xsd2"]
  }
}