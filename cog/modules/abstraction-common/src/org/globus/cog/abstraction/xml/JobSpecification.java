/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: JobSpecification.java,v 1.1 2005/04/22 09:53:24 amin Exp $
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
public class JobSpecification implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    private java.lang.String _executable;

    private java.lang.String _directory;

    private java.lang.String _arguments;

    private java.lang.String _stdOutput;

    private java.lang.String _stdInput;

    private java.lang.String _stdError;

    private int _count;

    /**
     * keeps track of state for field: _count
    **/
    private boolean _has_count;

    private boolean _batchJob;

    /**
     * keeps track of state for field: _batchJob
    **/
    private boolean _has_batchJob;

    private boolean _redirected;

    /**
     * keeps track of state for field: _redirected
    **/
    private boolean _has_redirected;

    private boolean _localExecutable;

    /**
     * keeps track of state for field: _localExecutable
    **/
    private boolean _has_localExecutable;

    /**
     * List of attributes defined as "name" "value" pairs 
    **/
    private AttributeList _attributeList;


      //----------------/
     //- Constructors -/
    //----------------/

    public JobSpecification() {
        super();
    } //-- org.globus.cog.core.xml.JobSpecification()


      //-----------/
     //- Methods -/
    //-----------/

    /**
    **/
    public void deleteBatchJob()
    {
        this._has_batchJob= false;
    } //-- void deleteBatchJob() 

    /**
    **/
    public void deleteCount()
    {
        this._has_count= false;
    } //-- void deleteCount() 

    /**
    **/
    public void deleteLocalExecutable()
    {
        this._has_localExecutable= false;
    } //-- void deleteLocalExecutable() 

    /**
    **/
    public void deleteRedirected()
    {
        this._has_redirected= false;
    } //-- void deleteRedirected() 

    /**
    **/
    public java.lang.String getArguments()
    {
        return this._arguments;
    } //-- java.lang.String getArguments() 

    /**
    **/
    public AttributeList getAttributeList()
    {
        return this._attributeList;
    } //-- AttributeList getAttributeList() 

    /**
    **/
    public boolean getBatchJob()
    {
        return this._batchJob;
    } //-- boolean getBatchJob() 

    /**
    **/
    public int getCount()
    {
        return this._count;
    } //-- int getCount() 

    /**
    **/
    public java.lang.String getDirectory()
    {
        return this._directory;
    } //-- java.lang.String getDirectory() 

    /**
    **/
    public java.lang.String getExecutable()
    {
        return this._executable;
    } //-- java.lang.String getExecutable() 

    /**
    **/
    public boolean getLocalExecutable()
    {
        return this._localExecutable;
    } //-- boolean getLocalExecutable() 

    /**
    **/
    public boolean getRedirected()
    {
        return this._redirected;
    } //-- boolean getRedirected() 

    /**
    **/
    public java.lang.String getStdError()
    {
        return this._stdError;
    } //-- java.lang.String getStdError() 

    /**
    **/
    public java.lang.String getStdInput()
    {
        return this._stdInput;
    } //-- java.lang.String getStdInput() 

    /**
    **/
    public java.lang.String getStdOutput()
    {
        return this._stdOutput;
    } //-- java.lang.String getStdOutput() 

    /**
    **/
    public boolean hasBatchJob()
    {
        return this._has_batchJob;
    } //-- boolean hasBatchJob() 

    /**
    **/
    public boolean hasCount()
    {
        return this._has_count;
    } //-- boolean hasCount() 

    /**
    **/
    public boolean hasLocalExecutable()
    {
        return this._has_localExecutable;
    } //-- boolean hasLocalExecutable() 

    /**
    **/
    public boolean hasRedirected()
    {
        return this._has_redirected;
    } //-- boolean hasRedirected() 

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
     * @param arguments
    **/
    public void setArguments(java.lang.String arguments)
    {
        this._arguments = arguments;
    } //-- void setArguments(java.lang.String) 

    /**
     * 
     * @param attributeList
    **/
    public void setAttributeList(AttributeList attributeList)
    {
        this._attributeList = attributeList;
    } //-- void setAttributeList(AttributeList) 

    /**
     * 
     * @param batchJob
    **/
    public void setBatchJob(boolean batchJob)
    {
        this._batchJob = batchJob;
        this._has_batchJob = true;
    } //-- void setBatchJob(boolean) 

    /**
     * 
     * @param count
    **/
    public void setCount(int count)
    {
        this._count = count;
        this._has_count = true;
    } //-- void setCount(int) 

    /**
     * 
     * @param directory
    **/
    public void setDirectory(java.lang.String directory)
    {
        this._directory = directory;
    } //-- void setDirectory(java.lang.String) 

    /**
     * 
     * @param executable
    **/
    public void setExecutable(java.lang.String executable)
    {
        this._executable = executable;
    } //-- void setExecutable(java.lang.String) 

    /**
     * 
     * @param localExecutable
    **/
    public void setLocalExecutable(boolean localExecutable)
    {
        this._localExecutable = localExecutable;
        this._has_localExecutable = true;
    } //-- void setLocalExecutable(boolean) 

    /**
     * 
     * @param redirected
    **/
    public void setRedirected(boolean redirected)
    {
        this._redirected = redirected;
        this._has_redirected = true;
    } //-- void setRedirected(boolean) 

    /**
     * 
     * @param stdError
    **/
    public void setStdError(java.lang.String stdError)
    {
        this._stdError = stdError;
    } //-- void setStdError(java.lang.String) 

    /**
     * 
     * @param stdInput
    **/
    public void setStdInput(java.lang.String stdInput)
    {
        this._stdInput = stdInput;
    } //-- void setStdInput(java.lang.String) 

    /**
     * 
     * @param stdOutput
    **/
    public void setStdOutput(java.lang.String stdOutput)
    {
        this._stdOutput = stdOutput;
    } //-- void setStdOutput(java.lang.String) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.JobSpecification unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.JobSpecification) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.JobSpecification.class, reader);
    } //-- org.globus.cog.core.xml.JobSpecification unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
