from flask.json import jsonify
from backend.models.usertask import User
import time

import jwt
from flask import current_app, session, request
from jwt.exceptions import InvalidSignatureError, InvalidTokenError


def gen_auth_token(user_id: int, expire_time: float = 0) -> str:
    # Set default expire time if 0 to 1 hour after sign in.
    if expire_time == 0:
        expire_time = time.time() + 24 * 60 * 60

    return jwt.encode(
        {"user_id": str(user_id)}, current_app.config.get("JWT_SECRET"), algorithm="HS256"
    )


def check_auth_token(token: str) -> int:
    try:
        result = jwt.decode(token, current_app.config.get("JWT_SECRET"), algorithms=["HS256"])
    except (InvalidTokenError, InvalidSignatureError):
        return None
    else:
        # Check user exists
        id = int(result.get("user_id"))
        user = User.query.filter_by(id=id).first()
        if user:
            return user.id
        
        return None

from . import app


# Setup pre-request filters
@app.before_request
def user_auth_check():
    session["user"] = check_auth_token(request.headers.get("X-Authenticate"))


# Setup is authenticated check
def is_authenticated(f):
    def decorated(*args, **kwargs):
        if session["user"] is None:
            return jsonify({"error": "You must be authenticated to do this"}), 403
        
        return f(*args, **kwargs)
    
    return decorated