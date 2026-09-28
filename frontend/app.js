const pages={home:"Visão geral",pix:"PIX",cards:"Cartões",statement:"Extrato",investments:"Investimentos",settings:"Configurações"};
let moneyVisible=true,selectedInvestment=null,currentCard=null;
const brl=v=>Number(v).toLocaleString("pt-BR",{style:"currency",currency:"BRL"});
const $=s=>document.querySelector(s);

function showToast(text){const t=$("#toast");if(!t)return;t.textContent=text;t.classList.add("show");clearTimeout(window.toastTimer);window.toastTimer=setTimeout(()=>t.classList.remove("show"),2400)}
function setText(s,t){const e=$(s);if(e)e.textContent=t}
function setMoney(s,v){const e=$(s);if(!e)return;if(v===null||v===undefined||Number.isNaN(Number(v))){e.dataset.value="";e.textContent="Indisponível";e.classList.remove("hidden-money");return}e.dataset.value=String(v);e.textContent=brl(v);e.classList.toggle("hidden-money",!moneyVisible)}
function unavailable(s,t="Indisponível"){const e=$(s);if(e){e.dataset.value="";e.textContent=t;e.classList.remove("hidden-money")}}
function api(){if(!window.ViaAPI)throw new Error("Backend indisponível.");return window.ViaAPI}
function esc(v){return String(v??"").replaceAll("&","&amp;").replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll('"',"&quot;").replaceAll("'","&#039;")}

function go(page){
  document.querySelectorAll(".page").forEach(x=>x.classList.remove("active"));
  $(`#page-${page}`)?.classList.add("active");
  document.querySelectorAll(".nav-item").forEach(x=>x.classList.toggle("active",x.dataset.page===page));
  setText("#pageTitle",pages[page]||"Via");
  $("#sidebar")?.classList.remove("open");
}

async function loadAccount(){
  try{
    const a=await api().getAccount();
    setMoney(".balance-card .money",a.balance);
    setMoney("#page-statement .statement-summary > div:nth-child(3) .money",a.balance);
    setText(".balance-meta span:first-child",a.accountNumberMasked||"Conta disponível");
    setText(".status-dot",a.status==="active"?"Conta ativa":a.status||"Conta disponível");
    setText("#accountYield", a.yieldLabel || a.accountMessage || "Disponível");
  }catch(e){
    unavailable(".balance-card .money");
    unavailable("#page-statement .statement-summary > div:nth-child(3) .money");
    setText(".balance-meta span:first-child","Conta indisponível");
    setText(".status-dot","Indisponível");setText("#accountYield","Indisponível");
  }
}

function normalizeTx(r){return Array.isArray(r)?r:Array.isArray(r?.transactions)?r.transactions:Array.isArray(r?.data)?r.data:[]}
function txIcon(t){return({pix_sent:"↗",pix_received:"↙",card_purchase:"▣",deposit:"＋",investment:"⌁",redemption:"↙",subscription:"●"})[t]||"•"}
function renderTx(sel,list,limit){
  const c=$(sel); if(!c)return;
  const arr=limit?list.slice(0,limit):list;
  if(!arr.length){c.innerHTML='<div class="empty-state">Nenhuma transação encontrada.</div>';return}
  c.innerHTML=arr.map(t=>{
    const a=Number(t.amount||0);
    const d=t.description||t.desc||t.category||"Movimentação";
    const dt=t.createdAt?new Date(t.createdAt).toLocaleString("pt-BR",{dateStyle:"short",timeStyle:"short"}):"";
    return `<div class="transaction"><div class="tx-icon">${txIcon(t.type)}</div><div><strong>${esc(t.name||t.title||"Transação")}</strong><small>${esc(d)}${dt?` • ${esc(dt)}`:""}</small></div><strong class="amount ${a>0?"positive":"negative"}">${a>0?"+":"-"} ${brl(Math.abs(a))}</strong></div>`
  }).join("");
}
async function loadTransactions(period="30d"){
  try{
    const r=await api().getTransactions({period,page:1,limit:100});
    const tx=normalizeTx(r);
    renderTx("#homeTransactions",tx,5); renderTx("#statementTransactions",tx);
    const inc=tx.filter(x=>Number(x.amount)>0).reduce((s,x)=>s+Number(x.amount),0);
    const out=Math.abs(tx.filter(x=>Number(x.amount)<0).reduce((s,x)=>s+Number(x.amount),0));
    setText("#statementIncome",`+ ${brl(inc)}`); setText("#statementExpenses",`- ${brl(out)}`);
  }catch(e){
    $("#homeTransactions").innerHTML='<div class="empty-state">Transações indisponíveis.</div>';
    $("#statementTransactions").innerHTML='<div class="empty-state">Extrato indisponível.</div>';
    setText("#statementIncome","Indisponível");setText("#statementExpenses","Indisponível");
  }
}


async function loadSpendingAnalytics(){
  try{
    const data=await api().getSpendingAnalytics("30d");
    setMoney("#spendingTotal", data.total);
    const categories=Array.isArray(data.categories)?data.categories:[];
    const c=$("#spendingCategories");
    if(!categories.length){
      c.innerHTML='<div class="empty-state">Nenhum gasto categorizado.</div>';
      return;
    }
    const max=Math.max(...categories.map(x=>Number(x.amount)||0),1);
    c.innerHTML=categories.map(x=>{
      const amount=Number(x.amount)||0;
      const width=Math.max(0,Math.min(100,(amount/max)*100));
      return `<div class="bar-row"><span>${esc(x.name||"Outros")}</span><div><i style="width:${width}%"></i></div><strong>${brl(amount)}</strong></div>`;
    }).join("");
  }catch(e){
    unavailable("#spendingTotal");
    const c=$("#spendingCategories");
    if(c)c.innerHTML='<div class="empty-state">Gastos indisponíveis.</div>';
  }
}

async function loadPixKeys(){
  try{
    const r=await api().getPixKeys(); const keys=Array.isArray(r)?r:r?.keys||r?.data||[];
    const c=$("#pixKeys");
    if(!keys.length){c.innerHTML='<div class="empty-state">Nenhuma chave PIX cadastrada.</div>';return}
    c.innerHTML=keys.map(k=>`<div><span>◇</span><div><strong>${esc(k.type||"Chave PIX")}</strong><small>${esc(k.maskedValue||k.value||"")}</small></div><button type="button" data-copy="${esc(k.value||"")}">Copiar</button></div>`).join("");
    c.querySelectorAll("[data-copy]").forEach(b=>b.onclick=async()=>{try{await navigator.clipboard.writeText(b.dataset.copy);showToast("Chave PIX copiada.")}catch{showToast("Não foi possível copiar.")}})
  }catch(e){$("#pixKeys").innerHTML='<div class="empty-state">Chaves PIX indisponíveis.</div>'}
}
async function loadPixFavorites(){
  try{
    const r=await api().getPixFavorites(); const f=Array.isArray(r)?r:r?.favorites||r?.data||[]; const c=$("#pixFavorites");
    if(!f.length){c.innerHTML='<div class="empty-state">Nenhum contato favorito.</div>';return}
    c.innerHTML=f.map(x=>{const i=(x.name||"?").split(" ").map(p=>p[0]).join("").slice(0,2).toUpperCase();return `<button type="button" class="favorite-contact" data-key="${esc(x.pixKey||x.key||"")}"><span>${esc(i)}</span><strong>${esc(x.name||"Contato")}</strong><small>${esc(x.maskedKey||x.pixKey||x.key||"")}</small></button>`}).join("");
    c.querySelectorAll(".favorite-contact").forEach(b=>b.onclick=()=>{$("#pixKey").value=b.dataset.key})
  }catch(e){$("#pixFavorites").innerHTML='<div class="empty-state">Contatos indisponíveis.</div>'}
}

const pixModal=$("#pixModal");
$("#pixForm")?.addEventListener("submit",e=>{
  e.preventDefault(); const key=$("#pixKey").value.trim(); const raw=$("#pixValue").value; const desc=$("#pixDescription").value.trim()||"Sem descrição"; const amount=Number(raw.replace(/\./g,"").replace(",","."));
  if(!key||!amount||amount<=0)return showToast("Confira a chave e o valor do PIX.");
  setText("#confirmPixTitle",`Enviar ${brl(amount)}`);setText("#confirmPixKey",key);setText("#confirmPixDescription",desc);
  pixModal.dataset.value=amount;pixModal.dataset.key=key;pixModal.dataset.desc=desc;pixModal.classList.add("show");
});
$("#confirmPixBtn")?.addEventListener("click",async()=>{
  try{
    await api().sendPix({key:pixModal.dataset.key,amount:Number(pixModal.dataset.value),description:pixModal.dataset.desc||""});
    pixModal.classList.remove("show");$("#pixForm").reset();showToast("PIX enviado com sucesso.");
    await Promise.all([loadAccount(),loadTransactions(),loadPixFavorites()]);
  }catch(e){showToast(e.message||"Não foi possível enviar o PIX.")}
});

function normalizeCards(r){return Array.isArray(r)?r:Array.isArray(r?.cards)?r.cards:Array.isArray(r?.data)?r.data:[]}
async function loadCards(){
  try{
    const cards=normalizeCards(await api().getCards()); currentCard=cards[0]||null;
    if(!currentCard)throw new Error();
    setText("#cardNumber",`••••  ••••  ••••  ${currentCard.last4||"••••"}`);
    setText("#cardHolder",currentCard.holderName||"Cliente Via");
    setText("#cardExpiration",currentCard.expiration||"—/—");
    setText("#cardBrand",(currentCard.brand||"").toUpperCase()||"—");
    setText("#cardProductName",currentCard.productName ? `CARTÃO ${String(currentCard.productName).toUpperCase()}` : "CARTÃO");
    setMoney("#page-cards .limit > div:first-child .money",currentCard.usedLimit);
    const av=currentCard.availableLimit??(currentCard.limit!=null&&currentCard.usedLimit!=null?Number(currentCard.limit)-Number(currentCard.usedLimit):null);
    setMoney("#page-cards .limit > div:last-child small:first-child .money",av);
    const ls=$("#page-cards .limit > div:last-child small:last-child");if(ls)ls.textContent=currentCard.limit!=null?`Limite: ${brl(currentCard.limit)}`:"Limite: Indisponível";
    const bar=$(".limit-bar i");if(bar)bar.style.width=currentCard.limit>0?`${Math.min(100,(Number(currentCard.usedLimit||0)/Number(currentCard.limit))*100)}%`:"0%";
    if($("#cardLock"))$("#cardLock").checked=!Boolean(currentCard.blocked);
    $("#bankCard")?.classList.toggle("locked",Boolean(currentCard.blocked));
  }catch(e){
    currentCard=null;setText("#cardNumber","•••• •••• •••• ••••");setText("#cardHolder","Indisponível");setText("#cardExpiration","—/—");setText("#cardBrand","—");setText("#cardProductName","CARTÃO");
    unavailable("#page-cards .limit > div:first-child .money");unavailable("#page-cards .limit > div:last-child small:first-child .money");
    const ls=$("#page-cards .limit > div:last-child small:last-child");if(ls)ls.textContent="Limite: Indisponível";
  }
}
$("#cardLock")?.addEventListener("change",async e=>{
  const blocked=!e.target.checked;
  if(!currentCard?.id){e.target.checked=!blocked;return showToast("Dados do cartão indisponíveis.")}
  try{await api().setCardBlocked(currentCard.id,blocked);showToast(blocked?"Cartão bloqueado.":"Cartão desbloqueado.");await loadCards()}
  catch(err){e.target.checked=blocked;showToast(err.message||"Não foi possível alterar o cartão.")}
});

async function loadInvestmentPortfolio(){
  try{
    const p=await api().getPortfolio();setMoney(".investment-total",p.total??p.balance);
    setText("#investmentReturn",p.monthlyReturn!=null?`${Number(p.monthlyReturn)>=0?"+":"-"} ${brl(Math.abs(Number(p.monthlyReturn)))} este mês`:"Indisponível");
  }catch(e){unavailable(".investment-total");setText("#investmentReturn","Indisponível")}
}
async function loadInvestmentProducts(){
  try{
    const r=await api().getInvestmentProducts();const p=Array.isArray(r)?r:r?.products||r?.data||[];const c=$("#investmentProducts");
    if(!p.length){c.innerHTML='<article class="investment-card"><h3>Nenhum investimento disponível</h3><p>Consulte novamente mais tarde.</p></article>';return}
    c.innerHTML=p.map(x=>`<article class="investment-card"><div class="investment-icon">${esc((x.name||"I")[0])}</div><span class="risk ${x.risk==="low"?"low":"mid"}">${esc(x.riskLabel||x.risk||"Risco informado pelo backend")}</span><h3>${esc(x.name||"Investimento")}</h3><p>${esc(x.description||"")}</p><strong>${esc(x.returnLabel||x.yieldLabel||"")}</strong><button class="secondary-btn invest-btn" data-id="${esc(x.id||"")}" data-name="${esc(x.name||"Investimento")}">Investir</button></article>`).join("");
    c.querySelectorAll(".invest-btn").forEach(b=>b.onclick=()=>{selectedInvestment={id:b.dataset.id,name:b.dataset.name};setText("#investTitle",b.dataset.name);$("#investModal").classList.add("show")})
  }catch(e){$("#investmentProducts").innerHTML='<article class="investment-card"><h3>Investimentos indisponíveis</h3><p>Não foi possível consultar o backend.</p></article>'}
}
$("#confirmInvestBtn")?.addEventListener("click",async()=>{
  if(!selectedInvestment?.id)return showToast("Investimento indisponível.");
  const amount=Number($("#investValue").value.replace(/\./g,"").replace(",","."));
  if(!amount||amount<=0)return showToast("Digite um valor válido.");
  try{await api().invest({productId:selectedInvestment.id,amount});$("#investModal").classList.remove("show");$("#investValue").value="";showToast("Investimento realizado.");await Promise.all([loadAccount(),loadTransactions(),loadInvestmentPortfolio()])}
  catch(e){showToast(e.message||"Não foi possível investir.")}
});

async function loadFinancialHealth(){
  try{
    const d=await api().getFinancialHealth();
    setText("#scoreValue",d.score??"—");
    setText("#financialHealthLabel",d.label||d.status||"Disponível");
    setText("#financialHealthDescription",d.description||"Dados atualizados pelo backend.");
    const ring=$("#scoreRing");
    if(ring){
      const score=Math.max(0,Math.min(1000,Number(d.score)||0));
      ring.style.strokeDashoffset=String(314-(314*(score/1000)));
    }
  }catch(e){
    setText("#scoreValue","—");
    setText("#financialHealthLabel","Indisponível");
    setText("#financialHealthDescription","Não foi possível consultar a saúde financeira.");
    const ring=$("#scoreRing"); if(ring) ring.style.strokeDashoffset="314";
  }
}
async function loadProfile(){
  try{
    const p=await api().getProfile();
    setText("#profileName",p.name||"Cliente Via");
    setText("#profileSince",p.customerSince?`Cliente Via desde ${new Date(p.customerSince).getFullYear()}`:"Cliente Via");
    const initials=(p.name||"").trim().split(/\s+/).filter(Boolean).map(x=>x[0]).join("").slice(0,2).toUpperCase()||"VB";
    setText("#profileAvatar",initials);
    setText("#topAvatar",initials);
  }catch(e){
    setText("#profileName","Indisponível");
    setText("#profileSince","Dados indisponíveis");
    setText("#profileAvatar","—");
    setText("#topAvatar","—");
  }
}
async function loadSettings(){
  try{
    const s=await api().getSettings();const t=document.querySelectorAll("#page-settings .switch input");
    if(t[0]&&s.compactMode!=null)t[0].checked=!!s.compactMode;if(t[1]&&s.notifications!=null)t[1].checked=!!s.notifications;
    if(s.hideBalance!=null){moneyVisible=!s.hideBalance;refreshMoneyVisibility()}
  }catch(e){}
}
async function saveSettings(){
  const t=document.querySelectorAll("#page-settings .switch input");
  try{await api().updateSettings({compactMode:!!t[0]?.checked,notifications:!!t[1]?.checked,hideBalance:!moneyVisible})}
  catch(e){showToast(e.message||"Não foi possível salvar as preferências.")}
}
function refreshMoneyVisibility(){
  document.querySelectorAll(".money").forEach(e=>{if(!e.dataset.value){e.classList.remove("hidden-money");return}e.classList.toggle("hidden-money",!moneyVisible);if(moneyVisible)e.textContent=brl(Number(e.dataset.value))})
}

document.querySelectorAll("[data-page]").forEach(b=>b.onclick=()=>go(b.dataset.page));
document.querySelectorAll("[data-go]").forEach(b=>b.onclick=()=>go(b.dataset.go));
$("#menuBtn")?.addEventListener("click",()=>$("#sidebar")?.classList.toggle("open"));
$("#eyeBtn")?.addEventListener("click",async()=>{moneyVisible=!moneyVisible;refreshMoneyVisibility();await saveSettings()});
document.querySelectorAll("[data-close]").forEach(b=>b.onclick=()=>b.closest(".modal-backdrop")?.classList.remove("show"));
document.querySelectorAll(".modal-backdrop").forEach(m=>m.onclick=e=>{if(e.target===m)m.classList.remove("show")});
document.querySelectorAll(".filters button").forEach(b=>b.onclick=async()=>{document.querySelectorAll(".filters button").forEach(x=>x.classList.remove("active"));b.classList.add("active");await loadTransactions(({"30 dias":"30d","90 dias":"90d","2026":"2026"})[b.textContent.trim()]||"30d")});
document.querySelectorAll("#page-settings .switch input").forEach(i=>i.onchange=saveSettings);
$("[data-action='deposit']")?.addEventListener("click",async()=>{
  const raw=prompt("Digite o valor do depósito:");if(raw===null)return;const amount=Number(raw.replace(/\./g,"").replace(",","."));
  if(!amount||amount<=0)return showToast("Digite um valor válido.");
  try{await api().deposit(amount,"Depósito realizado pelo site");showToast("Depósito enviado ao backend.");await Promise.all([loadAccount(),loadTransactions()])}
  catch(e){showToast(e.message||"Não foi possível realizar o depósito.")}
});
$("#logoutBtn")?.addEventListener("click",async()=>{
  try{await api().logout()}catch(e){ViaAPI.setAccessToken(null)}
  window.location.replace("login.html");
});
$("#notificationBtn")?.addEventListener("click",()=>showToast("Notificações dependem do backend."));

async function init(){
  if(!window.ViaAPI || !ViaAPI.getAccessToken()){
    window.location.replace("login.html");
    return;
  }

  try{
    await ViaAPI.getCurrentUser();
  }catch(e){
    if(e?.status===401 || e?.status===403){
      ViaAPI.setAccessToken(null);
      window.location.replace("login.html");
      return;
    }
  }

  await Promise.allSettled([
    loadAccount(),
    loadTransactions(),
    loadSpendingAnalytics(),
    loadPixKeys(),
    loadPixFavorites(),
    loadCards(),
    loadInvestmentPortfolio(),
    loadInvestmentProducts(),
    loadFinancialHealth(),
    loadProfile(),
    loadSettings()
  ]);

  try{await api().registerDevice()}catch(e){}
}
init();
