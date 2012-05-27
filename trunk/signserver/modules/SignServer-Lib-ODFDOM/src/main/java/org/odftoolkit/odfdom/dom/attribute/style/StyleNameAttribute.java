/************************************************************************
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. You can also
 * obtain a copy of the License at http://odftoolkit.org/docs/license.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ************************************************************************/

/*
 * This file is automatically generated.
 * Don't edit manually.
 */    
package org.odftoolkit.odfdom.dom.attribute.style;

import org.odftoolkit.odfdom.OdfFileDom;
import org.odftoolkit.odfdom.OdfName;
import org.odftoolkit.odfdom.OdfNamespace;
import org.odftoolkit.odfdom.dom.OdfNamespaceNames;
import org.odftoolkit.odfdom.OdfAttribute;
import org.odftoolkit.odfdom.OdfElement;
import org.odftoolkit.odfdom.dom.element.number.NumberBooleanStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberCurrencyStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberDateStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberNumberStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberPercentageStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberTextStyleElement;
import org.odftoolkit.odfdom.dom.element.number.NumberTimeStyleElement;
import org.odftoolkit.odfdom.dom.element.style.StyleFontFaceElement;
import org.odftoolkit.odfdom.dom.element.style.StyleMasterPageElement;
import org.odftoolkit.odfdom.dom.element.style.StylePageLayoutElement;
import org.odftoolkit.odfdom.dom.element.style.StylePresentationPageLayoutElement;
import org.odftoolkit.odfdom.dom.element.style.StyleStyleElement;
import org.odftoolkit.odfdom.dom.element.text.TextListStyleElement;
import org.odftoolkit.odfdom.dom.element.text.TextOutlineStyleElement;
     
/**
 * DOM implementation of OpenDocument attribute  {@odf.attribute style:name}.
 *
 */
public class StyleNameAttribute extends OdfAttribute {

	public static final OdfName ATTRIBUTE_NAME = OdfName.get( OdfNamespace.get(OdfNamespaceNames.STYLE), "name" );

	/**
	 * Create the instance of OpenDocument attribute {@odf.attribute style:name}.
	 * 
	 * @param ownerDocument       The type is <code>OdfFileDom</code>
	 */
	public StyleNameAttribute(OdfFileDom ownerDocument) {
		super(ownerDocument, ATTRIBUTE_NAME);
	}
	
	/**
	 * Returns the attribute name.
	 * 
	 * @return the <code>OdfName</code> for {@odf.attribute style:name}.
	 */
	@Override
	public OdfName getOdfName() {
		return ATTRIBUTE_NAME;
	}
	
	/**
	 * @return Returns the name of this attribute.
	 */
	@Override
	public String getName() {
		return ATTRIBUTE_NAME.getLocalName();
	}

	/**
	 * @param attrValue The <code>String</code> value of the attribute.
	 * @throws IllegalArgumentException If the provided attribute value is invalid
	 */
	@Override
	public void setValue(String attrValue) {
		OdfElement parentElement = (OdfElement)getOwnerElement();
		if( parentElement != null ){
			try {
				if( parentElement instanceof NumberBooleanStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof NumberCurrencyStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof NumberDateStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof NumberNumberStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof NumberPercentageStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof NumberTextStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof NumberTimeStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof StyleFontFaceElement ){
					super.setValue(attrValue);
				}else if( parentElement instanceof StyleMasterPageElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof StylePageLayoutElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof StylePresentationPageLayoutElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof StyleStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof TextListStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}else if( parentElement instanceof TextOutlineStyleElement ){
					//2DO: need validate value against StyleName
					super.setValue(attrValue);
				}			
			} catch (NullPointerException e) {
				// TODO: validation handling/logging
				throw new IllegalArgumentException(e);
			}
			  catch (IllegalArgumentException e) {
				// TODO: validation handling/logging
				throw (e);
			}
		}else{
			//2DO:what should do when the owner element is null?
			super.setValue(attrValue);
		}
	}
    
	/**
	 * @return Returns the <code>String</code> value of the attribute
	 */
	@Override
	public String getValue(){
		OdfElement parentElement = (OdfElement)getOwnerElement();
		if( parentElement != null ){
			try {
				if( parentElement instanceof NumberBooleanStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof NumberCurrencyStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof NumberDateStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof NumberNumberStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof NumberPercentageStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof NumberTextStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof NumberTimeStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof StyleFontFaceElement ){
					return super.getValue();
				}else if( parentElement instanceof StyleMasterPageElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof StylePageLayoutElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof StylePresentationPageLayoutElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof StyleStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof TextListStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}else if( parentElement instanceof TextOutlineStyleElement ){
					//2DO: need validate value against StyleName
					return super.getValue();
				}			
			} catch (IllegalArgumentException e) {
				// TODO: validation handling/logging
				throw new NumberFormatException("the value of style:name is not valid");
			}
		}else{
			//2DO:what should do when the owner element is null?
			return super.getValue();
		}
		return null;
	}
	
	/**
	 * Returns the default value of {@odf.attribute style:name}.
	 * 
	 * @return the default value as <code>String</code> dependent of its element name
	 *         return <code>null</code> if the default value does not exist
	 */
	@Override
	public String getDefault() {
		return null;
	}
	
	/**
	 * Default value indicator. As the attribute default value is dependent from its element, the attribute has only a default, when a parent element exists.
	 * 
	 * @return <code>true</code> if {@odf.attribute style:name} has an element parent 
	 *         otherwise return <code>false</code> as undefined.
	 */
	@Override
	public boolean hasDefault() {
		return false;
	}
	
	/**
	 * @return Returns whether this attribute is known to be of type ID (i.e. xml:id ?)
	 */
	@Override
	public boolean isId() {
		return false;
    
	}
}
