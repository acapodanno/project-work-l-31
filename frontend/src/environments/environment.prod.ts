// Valore di default per la build di produzione: da sovrascrivere con l'URL
// reale del backend Spring Boot quando si effettua il deploy (finché il
// progetto resta a scopo didattico/locale, resta lo stesso dell'ambiente di
// sviluppo).
export const environment = {
  production: true,
  backendUrl: 'http://localhost:8080/api',
};
