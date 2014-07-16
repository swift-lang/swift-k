The fake provider can be used to benchmark applications based
on CoG abstractions. It almost immediately completes tasks
without actually running jobs, transferring files or doing
any filesystem operations.

Since the local provider can be implicit in higher level
applications, the fake provider also provides a (fake) 
implementation for the local provider. It should therefore
not be used in conjuction with the local provider.