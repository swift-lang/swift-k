
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: Dependency.java,v 1.1 2005/04/22 09:53:24 amin Exp $
 */

package org.globus.cog.abstraction.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * 
 * @version $Revision: 1.1 $ $Date: 2005/04/22 09:53:24 $
**/
public class Dependency implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _from;

    private java.lang.String _to;


      //----------------/
     //- Constructors -/
    //----------------/

    public Dependency() {
        super();
    } //-- org.globus.cog.core.xml.Dependency()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public java.lang.String getFrom()
    {
        return this._from;
    } //-- java.lang.String getFrom() 

    /**
    **/
    public java.lang.String getTo()
    {
        return this._to;
    } //-- java.lang.String getTo() 

    /**
    **/
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * 
     * @param out
    **/
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * 
     * @param handler
    **/
    public void marshal(org.xml.sax.DocumentHandler handler)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.DocumentHandler) 

    /**
     * 
     * @param from
    **/
    public void setFrom(java.lang.String from)
    {
        this._from = from;
    } //-- void setFrom(java.lang.String) 

    /**
     * 
     * @param to
    **/
    public void setTo(java.lang.String to)
    {
        this._to = to;
    } //-- void setTo(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.Dependency unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.Dependency) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.Dependency.class, reader);
    } //-- org.globus.cog.core.xml.Dependency unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
