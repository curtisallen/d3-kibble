d3-kibble
=========

Rexter Extension for D3.js

Do a maven build `mvn package` place `target/d3-kibble-1.0.0-SNAPSHOT.jar` jar into your rexter ext directory.

Add the following to your rexser.xml graph config where you want to enable this kibble
```xml
<extensions>
	<allows>
		<allow>mis:*</allow>
	</allows>
</extensions>
```
Then you can access your graph data in a format that's easily pluggable into D3.js [force graph](http://bl.ocks.org/mbostock/4062045) for a visulaiton see [graphgen](https://github.com/curtisallen/graphgen)

`http://localhost:8182/graphs/<graph name>/mis/d3graph` will give D3.js reday json.

