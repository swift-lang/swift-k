/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: Specification.java,v 1.1 2005/04/22 09:53:24 amin Exp $
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
public class Specification implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private FileTransferSpecification _fileTransferSpecification;

    private JobSpecification _jobSpecification;


      //----------------/
     //- Constructors -/
    //----------------/

    public Specification() {
        super();
    } //-- org.globus.cog.core.xml.Specification()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public FileTransferSpecification getFileTransferSpecification()
    {
        return this._fileTransferSpecification;
    } //-- FileTransferSpecification getFileTransferSpecification() 

    /**
    **/
    public JobSpecification getJobSpecification()
    {
        return this._jobSpecification;
    } //-- JobSpecification getJobSpecification() 

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
     * @param fileTransferSpecification
    **/
    public void setFileTransferSpecification(FileTransferSpecification fileTransferSpecification)
    {
        this._fileTransferSpecification = fileTransferSpecification;
    } //-- void setFileTransferSpecification(FileTransferSpecification) 

    /**
     * 
     * @param jobSpecification
    **/
    public void setJobSpecification(JobSpecification jobSpecification)
    {
        this._jobSpecification = jobSpecification;
    } //-- void setJobSpecification(JobSpecification) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.Specification unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.Specification) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.Specification.class, reader);
    } //-- org.globus.cog.core.xml.Specification unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
