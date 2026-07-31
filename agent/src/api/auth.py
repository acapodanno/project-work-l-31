"""Verifica del token JWT emesso dal backend Spring Boot.

L'agente non ha un proprio sistema di login: riusa lo stesso token JWT che
il paziente/medico ottiene da `POST /api/auth/login` sul backend (stesso
secret HMAC, vedi `healthcare.jwt.secret` in application.properties e
`JWT_SECRET` in agent/.env). Espone la dipendenza FastAPI `get_current_user`
tramite `OAuth2PasswordBearer`, cosi' Swagger UI mostra correttamente lo
schema di sicurezza "Bearer" e dove ottenere il token.
"""
import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer

from ..config import settings

oauth2_scheme = OAuth2PasswordBearer(
    tokenUrl=f"{settings.backend_url}/auth/login",
    auto_error=True,
)


def get_current_user(token: str = Depends(oauth2_scheme)) -> str:
    """Verifica il JWT e ritorna l'email dell'utente (claim `sub`)."""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Token non valido o scaduto",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, settings.jwt_secret, algorithms=["HS256", "HS384", "HS512"])
    except jwt.PyJWTError:
        raise credentials_exception

    subject = payload.get("sub")
    if not subject:
        raise credentials_exception

    return subject
