from pydantic import BaseModel

class CycleRead(BaseModel):
    id: int
    cycle_name: str

    class Config:
        orm_mode = True