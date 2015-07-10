function highlightListing(el) {
	for (var i = 0; i < el.children.length; i++) {
		if (el.children[i].className == "content") {
			hljs.highlightBlock(el.children[i].children[0]);
		}
	}
}

function highlightAll(cls) {
	var els = document.getElementsByClassName(cls);
	for (var i = 0; i < els.length; i++) {
		highlightListing(els[i]);
	}
}

function initHl() {
	hljs.registerLanguage("swift", swiftHighlighter);
	hljs.registerLanguage("shell", shellHighlighter);
	hljs.registerLanguage("swiftconf", swiftConfHighlighter);
	hljs.registerLanguage("syntax", syntaxHighlighter);
	hljs.configure({languages: ["swift"]});
	highlightAll("listingblock swift");
	highlightAll("listingblock swiftdoc");
	hljs.configure({languages: ["shell"]});
	highlightAll("listingblock shell");
	hljs.configure({languages: ["swiftconf"]});
	highlightAll("listingblock swiftconf");
	hljs.configure({languages: ["syntax"]});
	highlightAll("listingblock syntax");
}
