function spring() {

var links = [
	    			{source:"Range", target:"Range",type:""},
	    			{source:"PurchaseableItem", target:"PurchaseableItem",type:""},
	    			{source:"Money", target:"Money",type:""},
	    			{source:"Window", target:"Window",type:""},
	    			{source:"PurchaseableItem", target:"Body",type:"suit"},
	    			{source:"PurchaseableItem", target:"Camera",type:"suit"},
	    			{source:"PurchaseableItem", target:"Lens",type:"suit"},
	    			{source:"Camera", target:"Large-Format",type:"suit"},
	    			{source:"Camera", target:"Digital",type:"suit"},
	    			{source:"Lens", target:"Lens",type:""},
	    			{source:"Camera", target:"Camera",type:""},
	    			{source:"Body", target:"Body",type:""},
	    			{source:"Money", target:"Money",type:""},
	    			{source:"BodyWithNonAdjustableShutterSpeed", target:"BodyWithNonAdjustableShutterSpeed",type:""},
	    			{source:"SLR", target:"SLR",type:""},
	    			{source:"Digital", target:"Digital",type:""},
	    			{source:"Large-Format", target:"Large-Format",type:""},
	    			{source:"Window", target:"Window",type:""},
	    			{source:"Range", target:"Range",type:""},
	    			{source:"PurchaseableItem", target:"PurchaseableItem",type:""},
	    			{source:"Viewer", target:"Viewer",type:""},
	];




var nodes = {};

// Compute the distinct nodes from the links.
links.forEach(function(link) {
  link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
  link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
});

var width = 1500,
    height = 700;

var force = d3.layout.force()
    .nodes(d3.values(nodes))
    .links(links)
    .size([width, height])
    .linkDistance(300)
    .charge(-750)
    .on("tick", tick)
    .start();

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height)
    .attr("id","main");

// Per-type markers, as they don't inherit styles.
svg.append("defs").selectAll("marker")
    .data(["suit", "licensing", "resolved"])
  .enter().append("marker")
    .attr("id", String)
    .attr("viewBox", "0 -5 10 10")
    .attr("refX", 15)
    .attr("refY", -1.5)
    .attr("markerWidth", 6)
    .attr("markerHeight", 6)
    .attr("orient", "auto")
  .append("path")
    .attr("d", "M0,-5L10,0L0,5");

var path = svg.append("g").selectAll("path")
    .data(force.links())
  	.enter().append("path")
    .attr("class", function(d) { return "link " + d.type; })
    .attr("marker-start", function(d) { return "url(#" + d.type + ")"; });




var circle = svg.append("g").selectAll("circle")
    .data(force.nodes())
  .enter().append("rect")
    .attr("x", 0)
    .attr("y", 0)
    .attr("width", 100)
    .attr("height", 50)
    .attr('stroke', 'black')
    .attr("id",function(d) { return d.name; })
    .attr("onmouseover","Tip(Test(evt),  BGCOLOR, 'white',FONTSIZE, '12pt',BORDERWIDTH, 0)")
    .attr("onmouseout","UnTip()")
    .style("fill","white")
    .call(force.drag);
    
    
//var node = vis.append("g").selectAll("circle.node")
//.data(nodes)
//.enter().append("svg:rect")                    
//.attr("x", function (d) { return d.x-25; })
//.attr("y", function (d) { return d.y-10; })
//.attr("width", 100)
//.attr("height", 50)
//.attr("rx", 6)
//.attr("ry", 6)
//.attr('stroke', 'black')
//.style("fill","white")
//.call(force.drag);
    
    
    
    

var text = svg.append("g").selectAll("g")
    .data(force.nodes())
  .enter().append("g");

// A copy of the text with a thick white stroke for legibility.
text.append("text")
    .attr("x", 10)
    .attr("y", 20)
    .style("font-size","14px")
    .text(function(d) { return d.name; });

text.append("text")
    .attr("x", 10)
    .attr("y", 20)
    .style("font-size","14px")
    .text(function(d) { return d.name; });

// Use elliptical arc path segments to doubly-encode directionality.
function tick() {
	
	path.attr("d", function(d) {
    var dx = d.target.x - d.source.x,
        dy = d.target.y - d.source.y,
        dr = Math.sqrt(dx * dx + dy * dy);
    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
  });
	
	
  circle.attr("transform", function(d) {
    return "translate(" + d.x + "," + d.y + ")";
  });

  text.attr("transform", function(d) {
	    return "translate(" + d.x + "," + d.y + ")";
});
}
}