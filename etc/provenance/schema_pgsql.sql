create table script_run (
	script_run_id	   text primary key,
        script_filename    text,
	log_filename       text,
	hostname	   text,
	script_run_dir	   text,
        swift_version      text,
        cog_version        text,
        final_state        text,
        start_time         text,
        duration           real
);

create table app_exec (
	app_exec_id			text primary key,
        script_run_id   		text references script_run(script_run_id),
	app__name			text,
	execution_site			text,
	start_time			text,
	duration			real,
	staging_in_duration		real,
	staging_out_duration		real,
	work_directory			text
);

create table argument (
	app_exec_id			text references app_exec (app_exec_id),
	arg_position			integer,
	app_exec_arg			text
);	

create table resource_usage (
       app_exec_id	    		text primary key references app_exec (app_exec_id),
       real_secs	       		real,
       kernel_secs             		real,
       user_secs	       		real,
       percent_cpu             		integer,
       max_rss	       	       		integer,
       avg_rss	       			integer,
       avg_tot_vm	       		integer,
       avg_priv_data     		integer,
       avg_priv_stack    		integer,
       avg_shared_text   		integer,
       page_size	       		integer,
       major_pgfaults    		integer,
       minor_pgfaults    		integer,
       swaps	       			integer,
       invol_context_switches		integer,
       vol_waits			integer,
       fs_reads				integer,
       fs_writes			integer,
       sock_recv			integer,
       sock_send			integer,
       signals				integer,
       exit_status			integer
);

create table file (
       file_id		text primary key,
       host		text, 
       name		text, 
       size		integer,
       modify		integer
);	

create table staged_in (
       app_exec_id			text references app_exec (app_exec_id),
       file_id 				text references file (file_id)
);

create table staged_out (
       app_exec_id			text references app_exec (app_exec_id),
       file_id				text references file (file_id)
);
