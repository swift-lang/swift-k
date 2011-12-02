# -------------------------------------------------------------------------- #
# Copyright 2010-2011, University of Chicago                                 #
#                                                                            #
# Licensed under the Apache License, Version 2.0 (the "License"); you may    #
# not use this file except in compliance with the License. You may obtain    #
# a copy of the License at                                                   #
#                                                                            #
# http://www.apache.org/licenses/LICENSE-2.0                                 #
#                                                                            #
# Unless required by applicable law or agreed to in writing, software        #
# distributed under the License is distributed on an "AS IS" BASIS,          #
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   #
# See the License for the specific language governing permissions and        #
# limitations under the License.                                             #
# -------------------------------------------------------------------------- #

"""
Persistent objects

A self-documenting, self-validating, persistent object library.
The only persistent backend currently supported is JSON, but
others could be added.
"""

from globus.provision.common.utils import enum

import inspect
import json

class ObjectValidationException(Exception):
    """A simple exception class used for validation exceptions"""
    pass

PropertyTypes = enum("STRING",
                     "INTEGER",
                     "NUMBER",
                     "BOOLEAN",
                     "OBJECT",
                     "ARRAY",
                     "NULL",
                     "ANY")

def pt_to_str(pt, items_type = None):
    if pt == PropertyTypes.STRING:
        return "string"
    elif pt == PropertyTypes.INTEGER:
        return "integer"
    elif pt == PropertyTypes.NUMBER:
        return "number"
    elif pt == PropertyTypes.BOOLEAN:
        return "boolean"
    elif pt == PropertyTypes.OBJECT:
        return "object"
    elif pt == PropertyTypes.ARRAY:
        return "list of %s" % pt_to_str(items_type)
    elif pt == PropertyTypes.NULL:
        return "null"
    elif pt == PropertyTypes.ANY:
        return "any"
    elif inspect.isclass(pt) and issubclass(pt, PersistentObject):
        return pt.__name__
    else:
        return "unknown"

def validate_property_type(value, expected_type, items_type = None, json = False):
    if expected_type == PropertyTypes.STRING:
        valid = isinstance(value, basestring)
    elif expected_type == PropertyTypes.INTEGER:
        valid = isinstance(value, int)
    elif expected_type == PropertyTypes.NUMBER:
        valid = isinstance(value, int) or isinstance(value, float)
    elif expected_type == PropertyTypes.BOOLEAN:
        valid = isinstance(value, bool)
    elif expected_type == PropertyTypes.OBJECT:
        valid = isinstance(value, dict)
    elif expected_type == PropertyTypes.ARRAY:
        if isinstance(value, list):
            valid = True
            for elem in value:
                valid &= validate_property_type(elem, items_type, json = json)
        elif isinstance(value, dict):
            valid = True
            for elem in value.values():
                valid &= validate_property_type(elem, items_type, json = json)
        else:
            valid = False    
    elif expected_type == PropertyTypes.NULL:
        valid = value is None
    elif expected_type == PropertyTypes.ANY:
        valid = True
    elif issubclass(expected_type, PersistentObject):
        # Further validation is done when we convert
        # this object
        if json:
            valid = isinstance(value, dict)
        else:
            valid = isinstance(value, expected_type)
    else:
        valid = False    
    
    return valid

class Property(object):    
    def __init__(self, name, proptype, required, description, editable = False, items = None, items_unique = False):
        self.name = name
        self.type = proptype
        self.required = required
        self.editable = editable
        self.items_unique = items_unique
        self.description = description
        self.items = items
        
        
class PropertyChange(object):
    ADD = 0
    REMOVE = 1
    EDIT = 2    
    
    def __init__(self, change_type):
        self.change_type = change_type


class PrimitivePropertyChange(PropertyChange):

    def __init__(self, change_type, old_value, new_value):
        PropertyChange.__init__(self, change_type)
        self.old_value = old_value
        self.new_value = new_value
        
    def to_dict(self):
        if self.change_type == PropertyChange.ADD:
            return {"__action__": "add"}
        elif self.change_type == PropertyChange.REMOVE:
            return {"__action__": "remove"}
        elif self.change_type == PropertyChange.EDIT:
            d = {}        
            d["old"] = self.old_value
            d["new"] = self.new_value
            return d
        
        
class ArrayPropertyChange(PropertyChange):
    
    def __init__(self, change_type, add, remove, edit):
        PropertyChange.__init__(self, change_type)
        self.add = add
        self.remove = remove
        self.edit = edit
        
    def to_dict(self):
        if self.change_type == PropertyChange.ADD:
            return {"__action__": "add"}
        elif self.change_type == PropertyChange.REMOVE:
            return {"__action__": "remove"}
        elif self.change_type == PropertyChange.EDIT:
            d = {}
        
            if len(self.add) > 0:
                d["ADD"] = self.add
                
            if len(self.remove) > 0:
                d["REMOVE"] = self.remove
            
            if len(self.edit) > 0:
                editd = {}
                for property in self.edit:
                    editd[property] = self.edit[property].to_dict()
                d["EDIT"] = editd
                
            return d
        
        
class ObjectPropertyChange(PropertyChange):
    
    def __init__(self, change_type, changes):
        PropertyChange.__init__(self, change_type)
        self.changes = changes
        
    def to_dict(self):
        if self.change_type == PropertyChange.ADD:
            return {"__action__": "add"}
        elif self.change_type == PropertyChange.REMOVE:
            return {"__action__": "remove"}
        elif self.change_type == PropertyChange.EDIT:
            d = {}
            for property in self.changes:
                d[property] = self.changes[property].to_dict()
            return d
        

class PersistentObject(object):
    def __init__(self):
        self._json_file = None

    def save(self, filename = None):
        if self._json_file == None and filename == None:
            raise Exception("Don't know where to save this topology")
        if filename != None:
            self._json_file = filename
        f = open (self._json_file, "w")
        json_string = self.to_json_string()
        f.write(json_string)
        f.close()
        
    def set_property(self, p_name, p_value):
        # TODO: Validation
        setattr(self, p_name, p_value)
        
    def has_property(self, p_name):
        return hasattr(self, p_name)       
    
    def get_property(self, p_name):
        # TODO: Validation
        return getattr(self, p_name)        
    
    def add_to_array(self, p_name, item_value):
        if not self.properties.has_key(p_name):
            raise ObjectValidationException("%s does not have a %s property" % (type(self).__name__, p_name))
        
        p = self.properties[p_name]
        
        if p.type != PropertyTypes.ARRAY:
            raise ObjectValidationException("Tried to add %s to %s.%s, but it is not an array." % (item_value, type(self).__name__, p_name))
        
        if not validate_property_type(item_value, p.items):
            raise ObjectValidationException("Tried to add %s to %s.%s, but this array contains %s." % (item_value, type(self).__name__, p_name, pt_to_str(p.items)))
        
        if p.items_unique and inspect.isclass(p.items) and issubclass(p.items, PersistentObject):
            if not self.has_property(p_name):
                p_value = {}
                self.set_property(p_name, p_value)
            else:
                p_value = self.get_property(p_name)
                
            setattr(item_value, "parent_%s" % type(self).__name__, self)
            p_value[item_value.id] = item_value
        else:
            if not self.has_property(p_name):
                p_value = []
                self.set_property(p_name, p_value)
            else:
                p_value = self.get_property(p_name)
                
            p_value.append(item_value)
            
    
    def validate_update(self, pobj):
        if type(self) != type(pobj):
            raise ObjectValidationException("Cannot update a %s object with a %s object" % (type(self).__name__, type(pobj).__name__))
        
        changes = {}
        for name, property in self.properties.items():
            self_hasattr = hasattr(self, name)
            pobj_hasattr = hasattr(pobj, name)
            
            if self_hasattr:
                self_value = getattr(self, name)

            if pobj_hasattr:
                pobj_value = getattr(pobj, name)
            
            if property.type in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                if not self_hasattr and pobj_hasattr:
                    if not property.editable:
                        raise ObjectValidationException("Tried to add a property, but it is non-editable (setting '%s' to %s)""" % (name, pobj_value))
                    else:
                        changes[name] = PrimitivePropertyChange(PropertyChange.ADD, None, pobj_value)
                elif self_hasattr and not pobj_hasattr:
                    if not property.editable:
                        raise ObjectValidationException("Tried to remove a property, but it is non-editable (removing '%s' = %s)""" % (name, self_value))
                    else:
                        changes[name] = PrimitivePropertyChange(PropertyChange.REMOVE, self_value, None)
                elif self_hasattr and pobj_hasattr:
                    # If this is a primitive type, check if the value has changed and, if so,
                    # whether the change is allowed.
                    if self_value != pobj_value:
                        if not property.editable:
                            raise ObjectValidationException("Tried to change the value of non-editable property '%s' (from %s to %s)""" % (name, self_value, pobj_value))
                        else:
                            changes[name] = PrimitivePropertyChange(PropertyChange.EDIT, self_value, pobj_value)
            elif property.type == PropertyTypes.ARRAY:
                if not self_hasattr and pobj_hasattr:
                    if not property.editable:
                        raise ObjectValidationException("Tried to add a property, but it is non-editable (setting '%s' to %s)""" % (name, pobj_value))
                    else:
                        changes[name] = ArrayPropertyChange(PropertyChange.ADD, None, None, None)
                elif self_hasattr and not pobj_hasattr:
                    if not property.editable:
                        raise ObjectValidationException("Tried to remove a property, but it is non-editable (removing '%s' = %s)""" % (name, self_value))
                    else:
                        changes[name] = ArrayPropertyChange(PropertyChange.REMOVE, None, None, None)
                elif self_hasattr and pobj_hasattr:
                    if property.items in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                        self_set = set(self_value)
                        pobj_set = set(pobj_value)
                        
                        add = list(pobj_set - self_set)
                        remove = list(self_set - pobj_set)
                        
                        if len(add) + len(remove) > 0:
                            if property.editable: 
                                changes[name] = ArrayPropertyChange(PropertyChange.EDIT, add, remove, {})
                            else:                        
                                raise ObjectValidationException("Tried to add/remove items from non-editable array '%s' (Add: %s  Remove: %s)""" % (name, add, remove))
                    elif inspect.isclass(property.items) and issubclass(property.items, PersistentObject):
                        if property.items_unique:
                            self_set = set(self_value.keys())
                            pobj_set = set(pobj_value.keys())
                            
                            add = list(pobj_set - self_set)
                            remove = list(self_set - pobj_set)
                            
                            if len(add) + len(remove) > 0 and not property.editable:
                                raise ObjectValidationException("Tried to add/remove items from non-editable array '%s' (Add: %s  Remove: %s)""" % (name, add, remove))                            
                            
                            common = list(self_set & pobj_set)
    
                            self_items_value = dict([(k, v) for k, v in self_value.items() if k in common])
                            pobj_items_value = dict([(k, v) for k, v in pobj_value.items() if k in common])
                            
                            edit = {}
                            for s in self_items_value.values():
                                p = pobj_items_value[s.id]
                                item_changes = s.validate_update(p)
                                if len(item_changes.changes) > 0:
                                    if not property.editable:
                                        raise ObjectValidationException("Tried to edit an item in an non-editable array '%s' (Item with id '%s')""" % (name, s.id))
                                    else:
                                        edit[s.id] = item_changes
    
                            if len(add) + len(remove) + len(edit) > 0:
                                changes[name] = ArrayPropertyChange(PropertyChange.EDIT, add, remove, edit)
                        else:
                            # We have no way of telling if individual entries have been edited,
                            # or even if entries have been added/removed, since we don't have
                            # object equality implemented yet.
                            pass
                    elif property.items in (PropertyTypes.ARRAY):
                        raise ObjectValidationException("ARRAYs of ARRAYs not supported.")                            
                    elif property.items in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                        raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.")                                                
            elif issubclass(property.type, PersistentObject):
                if not self_hasattr and pobj_hasattr:
                    if not property.editable:
                        raise ObjectValidationException("Tried to add a property, but it is non-editable (setting '%s' to %s)""" % (name, pobj_value))
                    else:
                        changes[name] = ObjectPropertyChange(PropertyChange.ADD, None)
                elif self_hasattr and not pobj_hasattr:
                    if not property.editable:
                        raise ObjectValidationException("Tried to remove a property, but it is non-editable (removing '%s' = %s)""" % (name, self_value))
                    else:
                        changes[name] = ObjectPropertyChange(PropertyChange.REMOVE, None)
                elif self_hasattr and pobj_hasattr:                 
                    property_changes = self_value.validate_update(pobj_value)
                    if len(property_changes.changes) > 0:
                        if not property.editable:
                            raise ObjectValidationException("Tried to to change the value of non-editable property '%s' (Changes: '%s')""" % (name, property_changes.to_dict()))
                        else:
                            changes[name] = property_changes
            elif property.type in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.") 
                
        return ObjectPropertyChange(PropertyChange.EDIT, changes)               

    def to_json_dict(self):
        json = {}
        for name, property in self.properties.items():
            if hasattr(self, name):
                if property.type in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                    value = getattr(self, name)
                elif property.type == PropertyTypes.ARRAY:
                    value = []
                    
                    if inspect.isclass(property.items) and issubclass(property.items, PersistentObject) and property.items_unique:
                        l = getattr(self, name).values()
                    else:
                        l = getattr(self, name)
                    
                    for elem in l:
                        if property.items in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                            value.append(elem)
                        elif issubclass(property.items, PersistentObject):
                            elem_obj = elem.to_json_dict()
                            value.append(elem_obj)
                        elif property.items in (PropertyTypes.ARRAY):
                            raise ObjectValidationException("ARRAYs of ARRAYs not supported.")                            
                        elif property.items in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                            raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.")
                elif issubclass(property.type, PersistentObject):
                    value = getattr(self, name).to_json_dict()              
                elif property.type in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                    raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.")
                json[name] = value
                
        return json

    def to_json_string(self):
        return json.dumps(self.to_json_dict(), indent=2)

    def __primitive_to_ruby(self, value, p_type):
        if p_type == PropertyTypes.STRING:
            return "\"%s\"" % value
        elif p_type == PropertyTypes.INTEGER:
            return "%i" % value
        elif p_type == PropertyTypes.NUMBER:
            return "%f" % value
        elif p_type == PropertyTypes.BOOLEAN:
            if value == True:
                return "true"
            else:
                return "false" 
        elif p_type == PropertyTypes.NULL:
            return "nil"        

    def to_ruby_hash_string(self):
        hash_str = "{"
        
        obj_items = {}
        for name, property in self.properties.items():
            if hasattr(self, name):
                value = getattr(self, name)
                if property.type in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                    value_str = self.__primitive_to_ruby(value, property.type)
                elif property.type == PropertyTypes.ARRAY and inspect.isclass(property.items) and issubclass(property.items, PersistentObject) and property.items_unique:
                    value_str = "{"
                        
                    items = {}
                    for k, elem in value.items():
                        items[k] = elem.to_ruby_hash_string()
                        
                    value_str += ", ".join([" \"%s\" => %s" % (k,v) for k,v in items.items()])
                    value_str += "}"
                elif property.type == PropertyTypes.ARRAY:
                    value_str = "["
                        
                    items = []
                    for elem in value:
                        if property.items in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                            items.append( self.__primitive_to_ruby(elem, property.items) )
                        elif issubclass(property.items, PersistentObject):
                            items.append( elem.to_ruby_hash_string() )
                        elif property.items in (PropertyTypes.ARRAY):
                            raise ObjectValidationException("ARRAYs of ARRAYs not supported.")                            
                        elif property.items in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                            raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.")

                    value_str += ", ".join(items)
                    value_str += "]"
                elif inspect.isclass(property.type) and issubclass(property.type, PersistentObject):
                    value_str = value.to_ruby_hash_string() 
                elif property.type in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                    raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.")
                obj_items[name] = value_str        

        hash_str += ", ".join([" :%s => %s" % (k,v) for k,v in obj_items.items()])

        hash_str += "}"
        
        return hash_str

    @classmethod
    def from_json_string(cls, json_string):
        try:
            json_dict = json.loads(json_string)
            return cls.from_json_dict(json_dict)
        except ValueError, ve:
            raise ObjectValidationException("Error parsing JSON. %s" % ve)

    @classmethod
    def from_json_dict(cls, obj_dict):
        obj = cls()
        if not isinstance(obj_dict, dict):
            raise ObjectValidationException("JSON provided for %s is not a dictionary" % cls.__name__)
        
        given_names = set(obj_dict.keys())
        required_names = set([p.name for p in cls.properties.values() if p.required])
        valid_names = set(cls.properties.keys())
        
        # Check whether required fields are present
        missing = required_names - given_names
        if len(missing) > 0:
            raise ObjectValidationException("JSON provided for %s is missing required properties: %s" % (cls.__name__, ", ".join(missing)))
        
        # Check whether there are any unexpected fields
        unexpected = given_names - valid_names
        if len(unexpected) > 0:
            raise ObjectValidationException("Encountered unexpected properties in JSON provided for %s: %s" % (cls.__name__, ", ".join(unexpected)))
        
        for p_name, p_value in obj_dict.items():
            property = cls.properties[p_name]
            if not validate_property_type(p_value, property.type, property.items, json = True):
                raise ObjectValidationException("'%s' is not a valid value for %s.%s. Expected a %s." % (p_value, cls.__name__, p_name, pt_to_str(property.type, property.items)))
            else:
                if property.type in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                    obj.set_property(p_name, p_value)
                    
                elif property.type == PropertyTypes.ARRAY and property.items_unique:
                    if property.items in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                        l = list(set(p_value))
                        if len(l) < len(p_value):
                            raise ObjectValidationException("%s.%s requires unique values, but '%s' contains duplicate values." % (cls.__name__, p_name, p_value))
                        obj.set_property(p_name, l)
                    if inspect.isclass(property.items) and issubclass(property.items, PersistentObject):
                        d = {}
                        for elem in p_value:
                            if not elem.has_key("id"):
                                raise ObjectValidationException("%s.%s requires unique objects, but '%s' does not have an 'id' property." % (cls.__name__, p_name, elem))
                            key = elem["id"]
                            if d.has_key(key):
                                raise ObjectValidationException("%s.%s requires unique objects, but id=%s encountered twice." % (cls.__name__, p_name, key))
                            elem_obj = property.items.from_json_dict(elem)
                            setattr(elem_obj, "parent_%s" % cls.__name__, obj)
                            d[key] = elem_obj
                        obj.set_property(p_name, d)
                    elif property.items in (PropertyTypes.ARRAY):
                        raise ObjectValidationException("ARRAYs of ARRAYs not supported.")                            
                    elif property.items in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                        raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.")     
                                           
                elif property.type == PropertyTypes.ARRAY and not property.items_unique:
                        if property.items in (PropertyTypes.STRING, PropertyTypes.INTEGER, PropertyTypes.NUMBER, PropertyTypes.BOOLEAN, PropertyTypes.NULL):
                            l = []
                            for elem in p_value:
                                l.append(elem)
                            obj.set_property(p_name, l)
                        elif inspect.isclass(property.items) and issubclass(property.items, PersistentObject):
                            l = []
                            for elem in p_value:
                                elem_obj = property.items.from_json_dict(elem)
                                l.append(elem_obj)
                            obj.set_property(p_name, l)
                        elif property.items in (PropertyTypes.ARRAY):
                            raise ObjectValidationException("ARRAYs of ARRAYs not supported.")                            
                        elif property.items in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                            raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.")
                        
                elif inspect.isclass(property.type) and issubclass(property.type, PersistentObject):
                    p_value_obj = property.type.from_json_dict(p_value)
                    obj.set_property(p_name, p_value_obj)               
                    
                elif property.type in (PropertyTypes.OBJECT, PropertyTypes.ANY):
                    raise ObjectValidationException("Arbitrary types (OBJECT, ANY) not supported.")
                
        return obj
