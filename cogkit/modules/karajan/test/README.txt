To run tests:
/path/to/karajan runtests.xml

To write a test:
Read the documentation.
Put an xml file in the tests directory.
Here's an example:

<test>
	<map> <!-- create a hashtable -->
		<map:entry key="name" value="NAME OF THE TEST"/>
		<map:entry key="test" value="{1}">
			<element>
				<!-- Whatever valid Karajan snippet -->
				<!-- It would be nice if it returns something -->
			</element>
		</map:entry>
		<!-- the following is optional. Should it be present, your test MUST
		return a value, which will be compared against the provided value -->
		<map:entry key="expected-result" value="20"/>
		<!-- this decides how the result and expected result are compared.
		There are three pre-defined methods:
			{default-comparison} (default - used if entry not present - <equals>)
			{math-comparison} (<math:equals>)
			{no-comparison} (<true> - test succeeds no matter what it returns)
			You can also provide your own comparison. It must accept the value1 and
			value2 arguments. -->
		<map:entry key="comparison" value="{math-comparison}"/>
		
		<!-- or -->
		
		<map:entry key="comparison">
			<element arguments="value1, value2">
				<math:equals value1="0" value2="{1}">
					<math:sum>
						<argument value="{value1}"/>
						<argument value="{value2}"/>
					</math:sum>
				</math:equals>
			</element>
		</map:entry>
		<!-- the above returns true if value1+value2=0 -->
		
	</map>
</test>

Also take a look at already existing tests.

The tests fail if:

An error is shown on the screen
OR
Nothing happens on the screen for a few minutes AND the tests are running
