import {readFileSync} from "node:fs";
import {stripTypeScriptTypes} from "node:module";
import test from "node:test";
import assert from "node:assert/strict";
const source = stripTypeScriptTypes(readFileSync(new URL("./calc.ts", import.meta.url), "utf8"), {mode:"transform"});
const {evaluate,format} = await import("data:text/javascript;base64," + Buffer.from(source).toString("base64"));
test("formatted scientific results remain usable",()=>{for(const n of [1e12,1e-10,-1e12]) {assert.equal(evaluate(format(n)),n);assert.equal(evaluate(format(n)+"+1"),n+1);}});
test("percent follows calculator arithmetic",()=>{for(const [s,n] of [["200+10%",220],["200−10%",180],["200×10%",20],["200+10%×2",200.2],["200+(10%)",200.1]]) assert.equal(evaluate(s),n,s);});
test("tangent poles report undefined",()=>assert.equal(evaluate("tan(90)"),null));
