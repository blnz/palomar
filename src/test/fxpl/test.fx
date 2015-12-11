<?xpsml?>
f:echo(xmlns:f='http://namespaces.blnz.com/fxpl') {
  f:context {
     param(name="result") {success;}
  }
  // let's see what we got
  f:paramRef(refParamName="result");
}