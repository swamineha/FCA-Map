/*
 * OntModelWrapper.java
 * Copyright (C) 2019 Guowei Chen <icgw@outlook.com>
 *
 * Distributed under terms of the GPL license.
 */

package cn.amss.semanticweb.model;

import java.util.Set;
import java.util.HashSet;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

import cn.amss.semanticweb.vocabulary.DBkWik;

/**
 * The wrapper of ontology model, which store the instances,
 * properites and classes.
 *
 * @author Guowei Chen (icgw@outlook.com)
 */
public class OntModelWrapper
{
  final Logger m_logger = LoggerFactory.getLogger(OntModelWrapper.class);

  private Model m_raw_model   = null;
  private OntModel m_ontology = null;

  private Set<Individual> m_instances   = null;
  private Set<OntProperty> m_properties = null;
  private Set<OntClass> m_classes       = null;

  /**
   * Intial member variables
   */
  public OntModelWrapper() {
    m_raw_model = ModelFactory.createDefaultModel();

    m_instances  = new HashSet<>();
    m_properties = new HashSet<>();
    m_classes    = new HashSet<>();
  }

  public OntModelWrapper(InputStream in) {
    this();
    read(in);
  }

  public OntModelWrapper(String file) {
    this();
    InputStream in = FileManager.get().open(file);

    if (null == in) {
      throw new IllegalArgumentException( "File: " + file + " not found.");
    }
    read(in);
  }

  private void read(InputStream in) {
    if (null == in) return;

    clear();

    m_raw_model.read(in, null);
    m_ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m_raw_model);

    acquireInstances();
    acquireProperties();
    acquireClasses();

    m_logger.info("#Instances: {}, #Properties: {}, #Classes: {}.",
        m_instances.size(), m_properties.size(), m_classes.size());
  }

  private static final boolean isSkipInstance(Individual i) {
    return i.hasProperty(RDF.type, SKOS.Concept) ||
           i.hasProperty(RDF.type, DBkWik.Image);
  }

  private static final boolean isSkipProperty(OntProperty p) {
    return false;
  }

  private static final boolean isSkipClass(OntClass c) {
    return false;
  }

  private void acquireInstances() {
    for (ExtendedIterator<Individual> it = m_ontology.listIndividuals(); it.hasNext(); ) {
      Individual i = it.next();
      if (isSkipInstance(i)) continue;
      m_instances.add(i);
    }
  }

  private void acquireProperties() {
    for (ExtendedIterator<OntProperty> it = m_ontology.listAllOntProperties(); it.hasNext(); ) {
      OntProperty p = it.next();
      if (isSkipProperty(p)) continue;
      m_properties.add(p);
    }
  }

  private void acquireClasses() {
    for (ExtendedIterator<OntClass> it = m_ontology.listClasses(); it.hasNext(); ) {
      OntClass c = it.next();
      if (isSkipClass(c)) continue;
      m_classes.add(c);
    }
  }

  private final void clear() {
    m_instances.clear();
    m_properties.clear();
    m_classes.clear();
  }

  public final void close() {
    if (m_raw_model != null && !m_raw_model.isClosed()) {
      m_raw_model.close();
    }

    if (m_ontology != null && !m_ontology.isClosed()) {
      m_ontology.close();
    }

    clear();
  }

  public Set<Individual> getInstances() {
    return m_instances;
  }

  public Set<OntProperty> getProperties() {
    return m_properties;
  }

  public Set<OntClass> getClasses() {
    return m_classes;
  }
}