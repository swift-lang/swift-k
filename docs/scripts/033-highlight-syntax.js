/*
Language: HTML, XML
Category: common
*/

syntaxHighlighter = function(hljs) {
  return {
    case_insensitive: false,
    lexemes: '(:=)|[\\|\\+\\*\\(\\)\\[\\]]',
    keywords: ':= + * ( ) [ ]',
    contains: [
      {
      	className: 'placeholder',
      	begin: '<', end: '>'
      },
      {
      	className: 'keyword',
      	begin: '\\|'
      },
      {
      	className: 'literal',
      	begin: "'", end: "'"
      },
    ]
  };
}
