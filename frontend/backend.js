const ViaAPI = (() => {
  const BASE_URL = "http://localhost:3000/api";

  let accessToken = localStorage.getItem("via_access_token") || null;

  function setAccessToken(token) {
    accessToken = token || null;

    if (token) {
      localStorage.setItem("via_access_token", token);
    } else {
      localStorage.removeItem("via_access_token");
    }
  }

  function getAccessToken() {
    return accessToken;
  }

  function headers(extra = {}) {
    const result = {
      "Content-Type": "application/json",
      Accept: "application/json",
      ...extra
    };

    if (accessToken) {
      result.Authorization = `Bearer ${accessToken}`;
    }

    return result;
  }

  async function request(endpoint, options = {}) {
    const config = {
      method: options.method || "GET",
      credentials: "include",
      ...options,
      headers: headers(options.headers)
    };

    if (options.body && typeof options.body !== "string") {
      config.body = JSON.stringify(options.body);
    }

    const response = await fetch(`${BASE_URL}${endpoint}`, config);
    const contentType = response.headers.get("content-type") || "";

    let data;
    if (contentType.includes("application/json")) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    if (!response.ok) {
      const message = data?.message || data?.error || `Erro HTTP ${response.status}`;
      const error = new Error(message);
      error.status = response.status;
      error.data = data;
      throw error;
    }

    return data;
  }

  // =============================
  // AUTENTICAÇÃO
  // =============================

  async function register(user) {
    const response = await request("/auth/register", {
      method: "POST",
      body: {
        name: user.name,
        cpf: user.cpf,
        email: user.email,
        phone: user.phone,
        birthDate: user.birthDate,
        password: user.password
      }
    });

    if (response?.accessToken) {
      setAccessToken(response.accessToken);
    }

    return response;
  }

  async function login(email, password) {
    const response = await request("/auth/login", {
      method: "POST",
      body: { email, password }
    });

    if (response?.accessToken) {
      setAccessToken(response.accessToken);
    }

    return response;
  }

  async function logout() {
    try {
      return await request("/auth/logout", { method: "POST" });
    } finally {
      setAccessToken(null);
    }
  }

  async function getCurrentUser() {
    return request("/auth/me");
  }

  // =============================
  // PERFIL
  // =============================

  async function getProfile() {
    return request("/profile");
  }

  async function updateProfile(profile) {
    return request("/profile", {
      method: "PUT",
      body: {
        name: profile.name,
        email: profile.email,
        phone: profile.phone,
        address: profile.address,
        city: profile.city,
        state: profile.state,
        zipCode: profile.zipCode
      }
    });
  }

  // =============================
  // CONTA / SALDO
  // =============================

  async function getAccount() {
    return request("/account");
  }

  async function getBalance() {
    return request("/account/balance");
  }

  async function deposit(amount, description = "") {
    return request("/account/deposit", {
      method: "POST",
      body: {
        amount: Number(amount),
        description
      }
    });
  }

  // =============================
  // PIX
  // =============================

  async function getPixKeys() {
    return request("/pix/keys");
  }

  async function createPixKey(type, value) {
    return request("/pix/keys", {
      method: "POST",
      body: { type, value }
    });
  }

  async function deletePixKey(keyId) {
    return request(`/pix/keys/${keyId}`, {
      method: "DELETE"
    });
  }

  async function sendPix({ key, amount, description = "" }) {
    return request("/pix/send", {
      method: "POST",
      body: {
        key,
        amount: Number(amount),
        description
      }
    });
  }

  async function getPixFavorites() {
    return request("/pix/favorites");
  }

  async function addPixFavorite(favorite) {
    return request("/pix/favorites", {
      method: "POST",
      body: {
        name: favorite.name,
        pixKey: favorite.pixKey,
        bankName: favorite.bankName || null
      }
    });
  }

  // =============================
  // TRANSAÇÕES / EXTRATO
  // =============================

  async function getTransactions({ period = "30d", page = 1, limit = 50 } = {}) {
    const params = new URLSearchParams({
      period,
      page: String(page),
      limit: String(limit)
    });

    return request(`/transactions?${params}`);
  }

  async function getTransaction(transactionId) {
    return request(`/transactions/${transactionId}`);
  }

  // =============================
  // CARTÕES
  // =============================

  async function getCards() {
    return request("/cards");
  }

  async function getCard(cardId) {
    return request(`/cards/${cardId}`);
  }

  async function setCardBlocked(cardId, blocked) {
    return request(`/cards/${cardId}/status`, {
      method: "PATCH",
      body: { blocked: Boolean(blocked) }
    });
  }

  async function updateCardLimit(cardId, newLimit) {
    return request(`/cards/${cardId}/limit`, {
      method: "PATCH",
      body: { limit: Number(newLimit) }
    });
  }

  async function getVirtualCard(cardId) {
    return request(`/cards/${cardId}/virtual`);
  }

  // IMPORTANTE:
  // Em um sistema financeiro real, não armazene CVV nem senha de cartão.
  // Prefira tokenização e um provedor compatível com PCI DSS.

  // =============================
  // INVESTIMENTOS
  // =============================

  async function getInvestmentProducts() {
    return request("/investments/products");
  }

  async function getPortfolio() {
    return request("/investments/portfolio");
  }

  async function invest({ productId, amount }) {
    return request("/investments", {
      method: "POST",
      body: {
        productId,
        amount: Number(amount)
      }
    });
  }

  async function redeemInvestment({ investmentId, amount }) {
    return request(`/investments/${investmentId}/redeem`, {
      method: "POST",
      body: { amount: Number(amount) }
    });
  }

  // =============================
  // ANALYTICS / GASTOS
  // =============================

  async function getSpendingAnalytics(period = "30d") {
    const params = new URLSearchParams({ period });
    return request(`/analytics/spending?${params}`);
  }

  // =============================
  // SAÚDE FINANCEIRA / SCORE
  // =============================

  async function getFinancialHealth() {
    return request("/financial-health");
  }

  // =============================
  // PREFERÊNCIAS
  // =============================

  async function getSettings() {
    return request("/settings");
  }

  async function updateSettings(settings) {
    return request("/settings", {
      method: "PUT",
      body: {
        compactMode: Boolean(settings.compactMode),
        notifications: Boolean(settings.notifications),
        hideBalance: Boolean(settings.hideBalance)
      }
    });
  }

  // =============================
  // DISPOSITIVO / SESSÃO
  // =============================

  async function registerDevice() {
    return request("/devices", {
      method: "POST",
      body: {
        userAgent: navigator.userAgent,
        language: navigator.language,
        platform: navigator.platform,
        screen: {
          width: window.screen.width,
          height: window.screen.height
        },
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
      }
    });
  }

  // =============================
  // ENVIO GENÉRICO
  // =============================

  async function sendData(endpoint, data, method = "POST") {
    return request(endpoint, {
      method,
      body: data
    });
  }

  // =============================
  // SINCRONIZAÇÃO COMPLETA
  // =============================

  async function syncDashboardState(state) {
    return request("/sync/dashboard", {
      method: "POST",
      body: {
        account: state.account || null,
        transactions: state.transactions || [],
        cards: state.cards || [],
        investments: state.investments || null,
        settings: state.settings || {},
        metadata: {
          sentAt: new Date().toISOString(),
          timezone: Intl.DateTimeFormat().resolvedOptions().timeZone
        }
      }
    });
  }

  // =============================
  // COLETA DE DADOS DO FRONT ATUAL
  // =============================

  function collectFrontendState() {
    const balanceElement = document.querySelector(".balance-card .money");
    const settingsSwitches = document.querySelectorAll("#page-settings .switch input");

    const hasBalance =
      balanceElement &&
      balanceElement.dataset.value !== undefined &&
      balanceElement.dataset.value !== "";

    return {
      account: hasBalance
        ? { balance: Number(balanceElement.dataset.value) }
        : null,

      transactions: [],
      cards: [],
      investments: null,

      settings: {
        compactMode: settingsSwitches[0]?.checked ?? null,
        notifications: settingsSwitches[1]?.checked ?? null
      }
    };
  }

  async function sendAllFrontendData() {
    return syncDashboardState(collectFrontendState());
  }

  return {
    setAccessToken,
    getAccessToken,
    request,

    register,
    login,
    logout,
    getCurrentUser,

    getProfile,
    updateProfile,

    getAccount,
    getBalance,
    deposit,

    getPixKeys,
    createPixKey,
    deletePixKey,
    sendPix,
    getPixFavorites,
    addPixFavorite,

    getTransactions,
    getTransaction,

    getCards,
    getCard,
    setCardBlocked,
    updateCardLimit,
    getVirtualCard,

    getInvestmentProducts,
    getPortfolio,
    invest,
    redeemInvestment,

    getSpendingAnalytics,
    getFinancialHealth,

    getSettings,
    updateSettings,

    registerDevice,
    sendData,
    collectFrontendState,
    syncDashboardState,
    sendAllFrontendData
  };
})();

window.ViaAPI = ViaAPI;
