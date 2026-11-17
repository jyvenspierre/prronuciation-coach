// Dictionary extension: open Free Dictionary API; not a licensed Dictionary.com feed.
const dictionaryWords={
stress:["testimony","photography","development","important","banana","computer","tomorrow","remember","ability","education","attention","beginning","experience","decision","community","information","beautiful","family","another","pronunciation"],
th:["think","three","through","thank","thought","thirty","thousand","Thursday","healthy","something","mother","father","brother","weather","together","other","those","these","there","although"],
r:["really","around","ready","right","remember","river","reading","return","career","correct","practice","program","report","reason","result","regular","arrive","very","sorry","world"],
vowels:["sheep","ship","seat","sit","beach","bit","food","good","cat","cut","bed","bad","pool","pull","cot","caught","luck","look","leave","live"]
};
const dictionaryCache=new Map();
let searchedWord=null, requestVersion=0;
async function dictionaryEntry(word){
 const key=word.toLowerCase().replace(/[^a-z'-]/g,"");
 if(!key)return null;
 if(dictionaryCache.has(key))return dictionaryCache.get(key);
 try{
  const response=await fetch("https://api.dictionaryapi.dev/api/v2/entries/en/"+encodeURIComponent(key));
  if(!response.ok)throw new Error("No entry");
  const entries=await response.json(),entry=entries[0];
  const pronunciations=entries.flatMap(e=>e.phonetics||[]);
  const american=pronunciations.find(p=>p.text&&/us|american/i.test(p.audio||""));
  const phonetic=american||pronunciations.find(p=>p.text);
  const result={ipa:phonetic?.text||entry.phonetic||"",definition:entry.meanings?.[0]?.definitions?.[0]?.definition||""};
  dictionaryCache.set(key,result);return result;
 }catch(e){dictionaryCache.set(key,null);return null}
}
const originalLesson=lesson;
lesson=function(){
 const category=el("skill").value,bank=dictionaryWords[category];
 const word=searchedWord||bank[idx%bank.length];
 const preset=lessons[category].find(x=>x.word.toLowerCase()===word.toLowerCase());
 return preset||{word,ipa:"",stress:"",tokens:[["I",""],["am",""],["practicing",""],[word,""],["today",""]]};
};
const originalRender=render;
render=async function(){
 const version=++requestVersion;
 originalRender();
 const item=lesson();
 el("dictionaryStatus").textContent="Looking up IPA and definition…";
 const entry=await dictionaryEntry(item.word);
 if(version!==requestVersion)return;
 if(entry){
  el("target").textContent=item.word+" "+(entry.ipa||item.ipa||"(IPA unavailable)");
  el("definition").textContent=entry.definition?"Definition: "+entry.definition:"";
  el("stress").textContent=item.stress?"Stress guide: "+item.stress:"Stress notation: "+(entry.ipa||"unavailable");
 }else{
  el("target").textContent=item.word+" "+(item.ipa||"(IPA unavailable)");
  el("definition").textContent="";
  el("stress").textContent=item.stress?"Stress guide: "+item.stress:"Stress notation unavailable";
 }
 const transcriptions=await Promise.all(item.tokens.map(async ([word,fallback])=>{
  const result=await dictionaryEntry(word);
  return result?.ipa||fallback||"IPA unavailable";
 }));
 if(version!==requestVersion)return;
 const buttons=el("words").querySelectorAll("button");
 buttons.forEach((button,i)=>{
  const small=button.querySelector("small");
  if(small)small.textContent=transcriptions[i];
  button.onclick=()=>{
   buttons.forEach(b=>b.classList.remove("active"));
   button.classList.add("active");
   el("detail").textContent=item.tokens[i][0]+" — "+transcriptions[i];
   say(item.tokens[i][0]);
  };
 });
 el("dictionaryStatus").textContent=entry?"Dictionary loaded. IPA may vary by dialect; US variants are preferred when identifiable.":"No dictionary entry found. Using built-in practice data where available.";
};
el("next").onclick=()=>{searchedWord=null;idx++;render()};
el("skill").onchange=()=>{searchedWord=null;idx=0;render()};
el("search").onclick=()=>{
 const word=el("lookup").value.trim().toLowerCase();
 if(!/^[a-z][a-z'-]*$/.test(word)){el("dictionaryStatus").textContent="Please enter a single English word.";return}
 searchedWord=word;render();
};
el("lookup").addEventListener("keydown",e=>{if(e.key==="Enter")el("search").click()});
render();
