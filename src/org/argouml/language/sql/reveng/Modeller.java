/* $Id$
 *******************************************************************************
 * Copyright (c) 2013 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent BRAUD (From Java module)
 *******************************************************************************
 */

package org.argouml.language.sql.reveng;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.argouml.application.api.Argo;

import org.argouml.kernel.ProjectManager;
import org.argouml.language.sql.ColumnDefinition;
import org.argouml.language.sql.ForeignKeyDefinition;
import org.argouml.language.sql.TableDefinition;
import org.argouml.model.Model;


import org.argouml.profile.Profile;

public class Modeller {
	private static final int ASSOCIATION = 1;
	private static final int GENERALIZATION = 2;
	
	// these next const need to be in a generic class
	private static final String ASSOCIATION_1 = "1";
	private static final String ASSOCIATION_01 = "0..1";
	
	
	/**
     * Current working model.
     */
    private Object model;

    /*
     * Sql profile model.
     *
    private Profile sqlProfile;
    */
    /**
     * The name of the file being parsed.
     */
    private String fileName;
    
    /**
     * New model elements that were created during this reverse engineering
     * session. TODO: We want a stronger type here, but ArgoUML treats all
     * elements as just simple Objects.
     */
    private Collection<Object> newElements;
    
    /**
     * 
     */
    private String settingLevel;
    
    
    private Map<String, TableDefinition> tablesByName;
    
    /////////
    public Modeller(Object theModel, Profile theSqlProfile,
            String theFileName) {
        model = theModel;
        //sqlProfile = theSqlProfile;
        
        newElements = new HashSet<Object>();
        fileName = theFileName;
        
        tablesByName = new HashMap<String, TableDefinition>();
        
        
    }
    
    private String getMappingDataTypeSQLToMCD(String typeSQL) {
    	String typeUML = "String";
		if (typeSQL.toLowerCase().indexOf("char") > 0) {
			// char, varchar, nvarchar [oracle], ...
			typeUML = "String";
		} else if (typeSQL.toLowerCase().indexOf("int") > 0) {
			// integer , *int,
			typeUML = "Integer";
		} else if (typeSQL.toLowerCase().indexOf("Boolean") > 0) {
			typeUML = "Boolean";
		} else if (typeSQL.toLowerCase().indexOf("text") > 0) {
			typeUML = "String";
		}
    	
    	return typeUML;
	}

	/**
     * Get the elements which were created while reverse engineering this file.
     * 
     * @return the collection of elements
     */
    public Collection<Object> getNewElements() {
        return newElements;
    }
    
	//////////////////////////
	/**
	* Call by the SqlParser
	* Build all elements
	*/
	public void generateModele() {
		
		Map<String, Object> classes = new HashMap<String, Object>();
		
		List<ForeignKeyDefinition> foreign_keys = new ArrayList<ForeignKeyDefinition>();
		// build the classes.
		for(TableDefinition table : tablesByName.values()) {
			Object curClass = addClass(table);
			classes.put(table.getName(), curClass);
			for(ColumnDefinition c : table.getColumnDefinitions()) {
				String attributeName = c.getName();
				String typeSpec = c.getDatatype();
	    		
				if (this.settingLevel.equals(SqlImportSettings.LEVEL_MCD)) {
	    			// Set a UML type instead of a type SQL (or with a tagValue)
					// getMappingDataTypeSQLToMCD(typeSpec);
					
					// Don't create attribute if a FK exists.
					// => reset attributeName to null
					for (ForeignKeyDefinition fk : table.getFkDefinitions()){
						if(fk.hasColumnInTable(attributeName)){
							attributeName = null;
							break;
						}
					}
	    		} else {
	    			// Stereotype,...	
	    		}
				
				// TODO : profile(SQL, or match with UML standard for conception) 
				Object packageOfType = this.model;
				Object mClassifierType = null;
				if (typeSpec != null) {
					mClassifierType = Model.getFacade().lookupIn(packageOfType, typeSpec);
					if (mClassifierType == null) {
						mClassifierType = Model.getCoreFactory().buildDataType(typeSpec, packageOfType);
						newElements.add(mClassifierType);
					}
				}
	    		//Object mClassifier = null;
				if (attributeName != null) {
					Object mAttribute = buildAttribute(curClass, mClassifierType, attributeName);
					String multiplicity = ASSOCIATION_1;
					if (c.getNullable() == null || c.getNullable()) {
						multiplicity = ASSOCIATION_01;
					}
					Model.getCoreHelper().setMultiplicity(mAttribute,
							multiplicity);
					
					if (c.getDefaultValue() != null) {
						Object newInitialValue = Model.getDataTypesFactory()
								.createExpression("Sql", c.getDefaultValue());
						Model.getCoreHelper().setInitialValue(mAttribute,
								newInitialValue);	
					}
					
				}
	    	}
			foreign_keys.addAll(table.getFkDefinitions());
		}
		
		for(ForeignKeyDefinition fk : foreign_keys) {
			String name = fk.getForeignKeyName();
			
			
			
			int typeAsso = ASSOCIATION;
			if (fk.getReferencesTable() != fk.getTable()) {
				List<String> fkTable = fk.getColumnNames();
				
				List<String> pkTable = fk.getTable().getPrimaryKeyFields();
				List<String> pkRef = fk.getReferencesTable().getPrimaryKeyFields();
				
				if (fkTable.size()>0 && pkTable.size()>0 && fkTable.containsAll(pkTable) && pkTable.containsAll(fkTable)) {
					
					if (pkRef.size()>0 && fkTable.containsAll(pkTable) && pkTable.containsAll(fkTable)) {
						typeAsso = GENERALIZATION;	
					}
				}
				
			}
			
			
			// if at least one is column of the FK, in the Table is nullable: "0..1", otherwise "1".
			String multiplicity = ASSOCIATION_1;
			for (ColumnDefinition columnDefinition : fk.getColumns()) {
				if (columnDefinition.getNullable() == null
						|| columnDefinition.getNullable()) {
					multiplicity = ASSOCIATION_01;
					break;
				}
			}
			
			
			
			// Build the good association type (association ?, composition ?, agregation ?, generalisation ?)
			Object mClassifier = classes.get(fk.getReferencesTable().getName());
			Object mClassifierEnd = classes.get(fk.getTable().getName());
			
			String nameAssociationEnd = name;
			
			if (typeAsso == GENERALIZATION) {
				Object mGeneralization = getGeneralization(this.model, mClassifier, mClassifierEnd);
				Model.getCoreHelper().setName(mGeneralization, nameAssociationEnd);
				
			} else if (typeAsso == ASSOCIATION) {
				Object mAssociationEnd = getAssociationEnd(name, mClassifier, mClassifierEnd);
				//setVisibility(mAssociationEnd, modifiers);
				Model.getCoreHelper().setMultiplicity(
	                  mAssociationEnd,
	                  multiplicity);
				Model.getCoreHelper().setType(mAssociationEnd, mClassifier);
				
				// String nameAssociationEnd = name;
				if (fk.getColumns().size() == 1) {
					nameAssociationEnd = fk.getColumns().get(0).getName();
				}
				
				Model.getCoreHelper().setName(mAssociationEnd, nameAssociationEnd);
				if (!mClassifier.equals(mClassifierEnd)) {
					// Because if they are equal,
					// then getAssociationEnd(name, mClassifier) could return
					// the wrong assoc end, on the other hand the navigability
					// is already set correctly (at least in this case), so the
					// next line is not necessary. (maybe never necessary?) - thn
					Model.getCoreHelper().setNavigable(mAssociationEnd, true);
				}
				//addDocumentationTag(mAssociationEnd, javadoc);*
				// else if (typeAsso == GENERALIZATION) {
				//}
			}
				
			
			
		}
		
	}
	
	/**
	 * 
	 * @param name : TODO can be null, not done.
	 * @param mClassifier
	 * @param mClassifierEnd
	 * @return
	 */
	private Object getAssociationEnd(String name, Object mClassifier, Object mClassifierEnd) {
        Object mAssociationEnd = null;
        for (Iterator<Object> i = Model.getFacade().getAssociationEnds(mClassifier)
                .iterator(); i.hasNext();) {
			Object ae = i.next();
			Object assoc = Model.getFacade().getAssociation(ae);
			if (name.equals(Model.getFacade().getName(ae))
					&& Model.getFacade().getConnections(assoc).size() == 2
					&& Model.getFacade().getType(Model.getFacade().getNextEnd(ae)) == mClassifierEnd) {
				mAssociationEnd = ae;
			}
        }
        if (mAssociationEnd == null) {
            Object mAssociation = buildDirectedAssociation(name, mClassifierEnd, mClassifier);
            // this causes a problem when mClassifier is not only 
            // at one assoc end: (which one is the right one?)
            mAssociationEnd =
                Model.getFacade().getAssociationEnd(
                        mClassifier,
                        mAssociation);
        }
        return mAssociationEnd;
    }
	
	/**
     * Build a unidirectional association between two Classifiers.(From SQL/JAVA)
     * 
     * @param name name of the association
     * @param sourceClassifier source classifier (end which is non-navigable)
     * @param destClassifier destination classifier (end which is navigable)
     * @return newly created Association
     */
    public static Object buildDirectedAssociation(String name,
            Object sourceClassifier, Object destClassifier) {
        return Model.getCoreFactory().buildAssociation(destClassifier, true,
                sourceClassifier, false, name);
    }
	
	/**
	 * Call by the SqlParser
	 * Must be call before the end of pasring a table (so, only when then name is known), because a FK can reference himself.
	 */
	public void addTable(TableDefinition table) {
		tablesByName.put(table.getName(), table);
	}
   
	public TableDefinition getTableFromName(String nameTable) {
		TableDefinition ret = tablesByName.get(nameTable);
		if (ret == null) {
			ret = new TableDefinition();
			ret.setName(nameTable);
			addTable(ret);
		}
		return ret;
	}

    
    private Object addClass(TableDefinition table) {
        
        Object mClass = addClassifier(Model.getCoreFactory().createClass(),
        		table.getName(), table.getComment(), null);

        /*Model.getCoreHelper().setAbstract(mClass,
                (modifiers & JavaParser.ACC_ABSTRACT) > 0);
        Model.getCoreHelper().setLeaf(mClass,
                (modifiers & JavaParser.ACC_FINAL) > 0);
        */
        if (Model.getFacade().getUmlVersion().charAt(0) == '1') {
            Model.getCoreHelper().setRoot(mClass, false);
        }
        newElements.add(mClass);
        return mClass;
    }
    
    private Object addClassifier(Object newClassifier, String name,
            String documentation, List<String> typeParameters) {
        Object mClassifier;
        Object mNamespace;

        Object currentPackage = this.model;
        
        
        mClassifier = Model.getFacade().lookupIn(currentPackage, name);
        mNamespace = currentPackage;

        if (mClassifier == null) {
            // if the classifier could not be found in the model
            //if (LOG.isInfoEnabled()) {
            //    LOG.info("Created new classifier for " + name);
            //}
            mClassifier = newClassifier;
            Model.getCoreHelper().setName(mClassifier, name);
            Model.getCoreHelper().setNamespace(mClassifier, mNamespace);
            newElements.add(mClassifier);
        }/* else {
            // it was found and we delete any existing tagged values.
            if (LOG.isInfoEnabled()) {
                LOG.info("Found existing classifier for " + name);
            }
            // TODO: Rewrite existing elements instead? - tfm
            cleanModelElement(mClassifier);
        } */

        /*
        // set up the artifact manifestation (only for top level classes)
        if (parseState.getClassifier() == null) {
            if (Model.getFacade().getUmlVersion().charAt(0) == '1') {
                // set the classifier to be a resident in its component:
                // (before we push a new parse state on the stack)
    
                // This test is carried over from a previous implementation,
                // but I'm not sure why it would already be set - tfm
                if (Model.getFacade()
                        .getElementResidences(mClassifier).isEmpty()) {
                    Object resident = Model.getCoreFactory()
                            .createElementResidence();
                    Model.getCoreHelper().setResident(resident, mClassifier);
                    Model.getCoreHelper().setContainer(resident,
                            parseState.getArtifact());
                }
            } else {
                Object artifact = parseState.getArtifact();
                Collection c =
                    Model.getCoreHelper().getUtilizedElements(artifact);
                if (!c.contains(mClassifier)) {
                    Object manifestation = Model.getCoreFactory()
                            .buildManifestation(mClassifier);
                    Model.getCoreHelper()
                            .addManifestation(artifact, manifestation);
                }
            }
        }*/

        /*
        // change the parse state to a classifier parse state
        parseStateStack.push(parseState);
        parseState = new ParseState(parseState, mClassifier, currentPackage);
		*/
        

        // Add classifier documentation tags during first (or only) pass only
        //if (getLevel() <= 0) {
            addDocumentationTag(mClassifier, documentation);
        //}
        // addTypeParameters(mClassifier, typeParameters);
        return mClassifier;
    }
    
    
    private void addDocumentationTag(Object modelElement, String sDocumentation) {
        if ((sDocumentation != null) && (sDocumentation.trim().length() >= 1)) {
        	//Now store documentation text in a tagged value
            String[] docs = {
            		sDocumentation
            };
            buildTaggedValue(modelElement, Argo.DOCUMENTATION_TAG, docs);
            addStereotypes(modelElement);
        }
    }
    
    private void buildTaggedValue(Object me, 
            String sTagName, 
            String[] sTagData) {
        Object tv = Model.getFacade().getTaggedValue(me, sTagName);
        if (tv == null) {
            // using deprecated buildTaggedValue here, because getting the tag
            // definition from a tag name is the critical step, and this is
            // implemented in ExtensionMechanismsFactory in a central place,
            // but not as a public method:
            Model.getExtensionMechanismsHelper().addTaggedValue(
                    me,
                    Model.getExtensionMechanismsFactory()
                    .buildTaggedValue(sTagName, sTagData[0]));
        } else {
            Model.getExtensionMechanismsHelper().setDataValues(tv, sTagData);
        }
    }
    
    private void addStereotypes(Object modelElement) {
        // TODO: What we do here is allowed for UML 1.x only!
        if (Model.getFacade().getUmlVersion().charAt(0) == '1') {
            Object tv = Model.getFacade()
                    .getTaggedValue(modelElement, "stereotype");
            if (tv != null) {
                String stereo = Model.getFacade().getValueOfTag(tv);
                if (stereo != null && stereo.length() > 0) {
                    StringTokenizer st = new StringTokenizer(stereo, ", ");
                    while (st.hasMoreTokens()) {
                        Model.getCoreHelper().addStereotype(modelElement,
                                getUML1Stereotype(st.nextToken().trim()));
                    }
                    ProjectManager.getManager().updateRoots();
                }
                Model.getUmlFactory().delete(tv);
            }
        }
    }
    
    /**
     * Get the stereotype with a specific name. UML 1.x only.
     * 
     * @param name The name of the stereotype.
     * @return The stereotype.
     */
    private Object getUML1Stereotype(String name) {
        //LOG.debug("Trying to find a stereotype of name <<" + name + ">>");
        // Is this line really safe wouldn't it just return the first
        // model element of the same name whether or not it is a stereotype
        Object stereotype = Model.getFacade().lookupIn(model, name);

        if (stereotype == null) {
            //LOG.debug("Couldn't find so creating it");
            return Model.getExtensionMechanismsFactory().buildStereotype(name,
                    model);
        }

        if (!Model.getFacade().isAStereotype(stereotype)) {
            // and so this piece of code may create an existing stereotype
            // in error.
            //LOG.debug("Found something that isn't a stereotype so creating it");
            return Model.getExtensionMechanismsFactory().buildStereotype(name,
                    model);
        }

        //LOG.debug("Found it");
        return stereotype;
    }
    
    private Object buildAttribute(Object classifier, Object type, String name) {
        Object mAttribute = Model.getCoreFactory().buildAttribute2(classifier,
                type);
        
        newElements.add(mAttribute);
        
        Model.getCoreHelper().setName(mAttribute, name);
        return mAttribute;
    }

	public void setLevel(String level) {
		this.settingLevel = level;
	}
    
	private Object getGeneralization(Object mPackage,
            Object parent,
            Object child) {
		Object mGeneralization = Model.getFacade().getGeneralization(child,
				parent);
		if (mGeneralization == null) {
			mGeneralization = Model.getCoreFactory().buildGeneralization(child,
					parent);
			newElements.add(mGeneralization);
		}
		if (mGeneralization != null) {
			Model.getCoreHelper().setNamespace(mGeneralization, mPackage);
		}
		return mGeneralization;
	}
}
