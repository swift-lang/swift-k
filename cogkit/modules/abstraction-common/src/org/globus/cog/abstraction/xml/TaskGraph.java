/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * This class was automatically generated with 
 * <a href="http://castor.exolab.org">Castor 0.9.2</a>, using an
 * XML Schema.
 * $Id: TaskGraph.java,v 1.1 2005/04/22 09:53:24 amin Exp $
 */

package org.globus.cog.abstraction.xml;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * 
 * @version $Revision: 1.1 $ $Date: 2005/04/22 09:53:24 $
**/
public class TaskGraph implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Identity represents a uniform resource name (URN) that
     * uniquely identifies a Grid entity
    **/
    private java.lang.String _identity;

    private java.lang.String _name;

    private java.lang.String _failureHandlingPolicy;

    /**
     * Representation of the abstract Java CoG Kit Task object.
    **/
    private java.util.Vector _taskList;

    private java.util.Vector _taskGraphList;

    /**
     * List of dependencies defined as "from" "to" pairs 
    **/
    private DependencyList _dependencyList;

    /**
     * List of attributes defined as "name" "value" pairs 
    **/
    private AttributeList _attributeList;

    private java.lang.String _status;

    private java.util.Date _submittedTime;

    private java.util.Date _completedTime;


      //----------------/
     //- Constructors -/
    //----------------/

    public TaskGraph() {
        super();
        _taskList = new Vector();
        _taskGraphList = new Vector();
    } //-- org.globus.cog.core.xml.TaskGraph()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * @param vTask
    **/
    public void addTask(Task vTask)
        throws java.lang.IndexOutOfBoundsException
    {
        _taskList.addElement(vTask);
    } //-- void addTask(Task) 

    /**
     * 
     * @param vTaskGraph
    **/
    public void addTaskGraph(TaskGraph vTaskGraph)
        throws java.lang.IndexOutOfBoundsException
    {
        _taskGraphList.addElement(vTaskGraph);
    } //-- void addTaskGraph(TaskGraph) 

    /**
    **/
    public java.util.Enumeration enumerateTask()
    {
        return _taskList.elements();
    } //-- java.util.Enumeration enumerateTask() 

    /**
    **/
    public java.util.Enumeration enumerateTaskGraph()
    {
        return _taskGraphList.elements();
    } //-- java.util.Enumeration enumerateTaskGraph() 

    /**
    **/
    public AttributeList getAttributeList()
    {
        return this._attributeList;
    } //-- AttributeList getAttributeList() 

    /**
    **/
    public java.util.Date getCompletedTime()
    {
        return this._completedTime;
    } //-- java.util.Date getCompletedTime() 

    /**
    **/
    public DependencyList getDependencyList()
    {
        return this._dependencyList;
    } //-- DependencyList getDependencyList() 

    /**
    **/
    public java.lang.String getFailureHandlingPolicy()
    {
        return this._failureHandlingPolicy;
    } //-- java.lang.String getFailureHandlingPolicy() 

    /**
    **/
    public java.lang.String getIdentity()
    {
        return this._identity;
    } //-- java.lang.String getIdentity() 

    /**
    **/
    public java.lang.String getName()
    {
        return this._name;
    } //-- java.lang.String getName() 

    /**
    **/
    public java.lang.String getStatus()
    {
        return this._status;
    } //-- java.lang.String getStatus() 

    /**
    **/
    public java.util.Date getSubmittedTime()
    {
        return this._submittedTime;
    } //-- java.util.Date getSubmittedTime() 

    /**
     * 
     * @param index
    **/
    public Task getTask(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _taskList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (Task) _taskList.elementAt(index);
    } //-- Task getTask(int) 

    /**
    **/
    public Task[] getTask()
    {
        int size = _taskList.size();
        Task[] mArray = new Task[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Task) _taskList.elementAt(index);
        }
        return mArray;
    } //-- Task[] getTask() 

    /**
    **/
    public int getTaskCount()
    {
        return _taskList.size();
    } //-- int getTaskCount() 

    /**
     * 
     * @param index
    **/
    public TaskGraph getTaskGraph(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _taskGraphList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return (TaskGraph) _taskGraphList.elementAt(index);
    } //-- TaskGraph getTaskGraph(int) 

    /**
    **/
    public TaskGraph[] getTaskGraph()
    {
        int size = _taskGraphList.size();
        TaskGraph[] mArray = new TaskGraph[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (TaskGraph) _taskGraphList.elementAt(index);
        }
        return mArray;
    } //-- TaskGraph[] getTaskGraph() 

    /**
    **/
    public int getTaskGraphCount()
    {
        return _taskGraphList.size();
    } //-- int getTaskGraphCount() 

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
    **/
    public void removeAllTask()
    {
        _taskList.removeAllElements();
    } //-- void removeAllTask() 

    /**
    **/
    public void removeAllTaskGraph()
    {
        _taskGraphList.removeAllElements();
    } //-- void removeAllTaskGraph() 

    /**
     * 
     * @param index
    **/
    public Task removeTask(int index)
    {
        Object obj = _taskList.elementAt(index);
        _taskList.removeElementAt(index);
        return (Task) obj;
    } //-- Task removeTask(int) 

    /**
     * 
     * @param index
    **/
    public TaskGraph removeTaskGraph(int index)
    {
        Object obj = _taskGraphList.elementAt(index);
        _taskGraphList.removeElementAt(index);
        return (TaskGraph) obj;
    } //-- TaskGraph removeTaskGraph(int) 

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
     * @param completedTime
    **/
    public void setCompletedTime(java.util.Date completedTime)
    {
        this._completedTime = completedTime;
    } //-- void setCompletedTime(java.util.Date) 

    /**
     * 
     * @param dependencyList
    **/
    public void setDependencyList(DependencyList dependencyList)
    {
        this._dependencyList = dependencyList;
    } //-- void setDependencyList(DependencyList) 

    /**
     * 
     * @param failureHandlingPolicy
    **/
    public void setFailureHandlingPolicy(java.lang.String failureHandlingPolicy)
    {
        this._failureHandlingPolicy = failureHandlingPolicy;
    } //-- void setFailureHandlingPolicy(java.lang.String) 

    /**
     * 
     * @param identity
    **/
    public void setIdentity(java.lang.String identity)
    {
        this._identity = identity;
    } //-- void setIdentity(java.lang.String) 

    /**
     * 
     * @param name
    **/
    public void setName(java.lang.String name)
    {
        this._name = name;
    } //-- void setName(java.lang.String) 

    /**
     * 
     * @param status
    **/
    public void setStatus(java.lang.String status)
    {
        this._status = status;
    } //-- void setStatus(java.lang.String) 

    /**
     * 
     * @param submittedTime
    **/
    public void setSubmittedTime(java.util.Date submittedTime)
    {
        this._submittedTime = submittedTime;
    } //-- void setSubmittedTime(java.util.Date) 

    /**
     * 
     * @param index
     * @param vTask
    **/
    public void setTask(int index, Task vTask)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _taskList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _taskList.setElementAt(vTask, index);
    } //-- void setTask(int, Task) 

    /**
     * 
     * @param taskArray
    **/
    public void setTask(Task[] taskArray)
    {
        //-- copy array
        _taskList.removeAllElements();
        for (int i = 0; i < taskArray.length; i++) {
            _taskList.addElement(taskArray[i]);
        }
    } //-- void setTask(Task) 

    /**
     * 
     * @param index
     * @param vTaskGraph
    **/
    public void setTaskGraph(int index, TaskGraph vTaskGraph)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _taskGraphList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _taskGraphList.setElementAt(vTaskGraph, index);
    } //-- void setTaskGraph(int, TaskGraph) 

    /**
     * 
     * @param taskGraphArray
    **/
    public void setTaskGraph(TaskGraph[] taskGraphArray)
    {
        //-- copy array
        _taskGraphList.removeAllElements();
        for (int i = 0; i < taskGraphArray.length; i++) {
            _taskGraphList.addElement(taskGraphArray[i]);
        }
    } //-- void setTaskGraph(TaskGraph) 

    /**
     * 
     * @param reader
    **/
    public static org.globus.cog.abstraction.xml.TaskGraph unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (org.globus.cog.abstraction.xml.TaskGraph) Unmarshaller.unmarshal(org.globus.cog.abstraction.xml.TaskGraph.class, reader);
    } //-- org.globus.cog.core.xml.TaskGraph unmarshal(java.io.Reader) 

    /**
    **/
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}
