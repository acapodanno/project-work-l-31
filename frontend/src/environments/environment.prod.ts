// Valori di default per la build di produzione: da sovrascrivere con gli URL
// reali del backend Spring Boot e dell'agente AI quando si effettua il deploy
// (finché il progetto resta a scopo didattico/locale, restano gli stessi
// dell'ambiente di sviluppo).
export const environment = {
  production: true,
  backendUrl: 'http://localhost:8080/api',
  agentUrl: 'http://localhost:5000/api',
};
