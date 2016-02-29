include "shared.thrift"

struct Work {
  1: string OP_TYPE,
  2: optional string OP_INFO
}

exception InvalidOperation {
  1: i32 what,
  2: string why
}

service RadicalPilotInterface {

  /**
   * A method definition looks like C code. It has a return type, arguments,
   * and optionally a list of exceptions that it may throw. Note that argument
   * lists and exception lists are specified using the exact same syntax as
   * field lists in struct or exception definitions.
   */

   string submit_task(1:string task_filename) throws    (1:InvalidOperation ouch),

   string status_task(1:string task_id) throws (1:InvalidOperation ouch),

   string cancel_task(1:string task_id) throws (1:InvalidOperation ouch),

}

