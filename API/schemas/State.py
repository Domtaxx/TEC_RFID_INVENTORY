from pydantic import BaseModel

class StateRead(BaseModel):
    id: int
    state_name: str

    class Config:
        orm_mode = True