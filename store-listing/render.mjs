/** Deterministic store artwork. UI captures and CSS artwork are separate stages. */
import {readFile,writeFile,mkdir,stat} from 'node:fs/promises';
import {createServer} from 'node:http';
import {fileURLToPath} from 'node:url';
import {resolve,dirname,extname,sep} from 'node:path';
import {parseArgs} from 'node:util';
import sharp from 'sharp';

const {values:args}=parseArgs({options:{capture:{type:'boolean'},validate:{type:'boolean'},theme:{type:'string'},palette:{type:'string'},style:{type:'string'},'app-theme':{type:'string'},group:{type:'string'},'text-only':{type:'boolean'}}});
const here=dirname(fileURLToPath(import.meta.url)),root=dirname(here),locale=resolve(here,'en-US');
const config=JSON.parse(await readFile(resolve(here,'design.json'),'utf8'));
const listing=JSON.parse(await readFile(resolve(locale,'listing.json'),'utf8'));
for(const [key,file,max]of [['title','title.txt',30],['shortDescription','short-description.txt',80],['fullDescription','full-description.txt',4000]]){
 if(!listing[key]||listing[key].length>max)throw Error(`${key} must contain 1–${max} characters`);
 await writeFile(resolve(locale,file),listing[key]+'\n');
}
if(args['text-only'])process.exit(0);
const manifest=[];
async function record(file,width,height){
 const full=resolve(locale,file),meta=await sharp(full).metadata(),info=await stat(full);
 if(meta.width!==width||meta.height!==height)throw Error(`Wrong dimensions: ${file}`);
 if(info.size>8*1024*1024)throw Error(`Image exceeds 8 MB: ${file}`);
 if(file.includes('feature')&&meta.hasAlpha)throw Error('Feature graphic must be opaque');
 manifest.push({file,width,height,bytes:info.size});
}
const groups=Object.entries(config.profiles).filter(([kind])=>!args.group||kind===args.group);
if(args.validate){
 for(const[kind,p]of groups)for(const s of config.slides.filter(s=>s.groups.includes(kind)))await record(`${kind}/${kind}-${s.id}.jpg`,p.width,p.height);
 await record('graphics/feature-graphic.png',1024,500);await record('graphics/app-icon.png',512,512);
 console.log(`Validated ${manifest.length} assets and all copy limits.`);process.exit(0);
}
const {chromium}=await import('playwright');
const mime={'.html':'text/html','.js':'text/javascript','.mjs':'text/javascript','.wasm':'application/wasm','.json':'application/json','.png':'image/png','.jpg':'image/jpeg','.svg':'image/svg+xml','.ttf':'font/ttf','.otf':'font/otf'};
const server=createServer(async(req,res)=>{
 try{let path=resolve(root,'.'+decodeURIComponent(new URL(req.url,'http://localhost').pathname));if(path!==root&&!path.startsWith(root+sep))throw Error('Invalid path');let info=await stat(path);if(info.isDirectory())path=resolve(path,'index.html');res.setHeader('Content-Type',mime[extname(path)]||'application/octet-stream');res.setHeader('Cache-Control','no-store');res.end(await readFile(path));}catch{res.statusCode=404;res.end('Not found');}
});
await new Promise(r=>server.listen(0,'127.0.0.1',r));const base=`http://127.0.0.1:${server.address().port}`;
const browser=await chromium.launch();
try{
 const page=await browser.newPage({deviceScaleFactor:1,locale:'en-US',acceptDownloads:true});
 for(const[kind,p]of groups){
  await mkdir(resolve(locale,'raw',kind),{recursive:true});await mkdir(resolve(locale,kind),{recursive:true});
  const slides=config.slides.filter(s=>s.groups.includes(kind));
  for(let i=0;i<slides.length;i++){
   const s=slides[i];await page.setViewportSize({width:p.width,height:p.height});
   if(args.capture){
    const q=new URLSearchParams({scene:s.scene,theme:args['app-theme']||s.theme||config.captureDefaults.theme,palette:args.palette||s.palette||config.captureDefaults.palette,style:args.style||config.captureDefaults.style,density:String(p.density)});
    await page.goto(`${base}/store-listing/capture-resources/?${q}`);
    await page.waitForFunction(()=>document.title.startsWith('Capture ready:'));
    await page.getByRole('button',{name:'Export capture PNG',exact:true}).click();
    const data=await page.getByRole('img',{name:'Exported Calcula capture',exact:true}).getAttribute('src');
    await writeFile(resolve(locale,'raw',kind,`${s.id}.png`),Buffer.from(data.split(',')[1],'base64'));
    const raw=await sharp(resolve(locale,'raw',kind,`${s.id}.png`)).stats();if(raw.isOpaque===false)throw Error('Canvas capture is transparent; verify export before publishing');
   }
   await page.goto(`${base}/store-listing/artwork.html?kind=${kind}&slide=${i}&theme=${args.theme||config.defaultTheme}`);
   await page.waitForFunction(()=>document.title.startsWith('Artwork ready:'));
   await page.screenshot({path:resolve(locale,kind,`${kind}-${s.id}.jpg`),type:'jpeg',quality:96});
   await record(`${kind}/${kind}-${s.id}.jpg`,p.width,p.height);console.log(`${kind}/${s.id}`);
  }
 }
 for(const[kind,width,height,file]of [['feature',1024,500,'feature-graphic.png'],['icon',512,512,'app-icon.png']]){
  await page.setViewportSize({width,height});await page.goto(`${base}/store-listing/artwork.html?kind=${kind}`);await page.waitForFunction(()=>document.title.startsWith('Artwork ready:'));
  const pixels=await page.screenshot({type:'png'});let pipeline=sharp(pixels);if(kind==='feature')pipeline=pipeline.flatten({background:config.brand.background}).removeAlpha();else pipeline=pipeline.ensureAlpha();
  await pipeline.png().toFile(resolve(locale,'graphics',file));await record(`graphics/${file}`,width,height);
 }
 await writeFile(resolve(locale,'assets.json'),JSON.stringify({generatedAt:new Date().toISOString(),theme:args.theme||config.defaultTheme,source:'Actual shared Compose UI with illustrative sample data; deterministic HTML/CSS framing',assets:manifest},null,2)+'\n');
 console.log(`Exported ${manifest.length} validated assets.`);
}finally{await browser.close();await new Promise(r=>server.close(r));}
