CREATE TABLE medicos (
    id UUID PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    telefono VARCHAR(30),
    email VARCHAR(254)
);

CREATE TABLE pacientes (
    id UUID PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    documento_identidad VARCHAR(50) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    email VARCHAR(254) NOT NULL,
    fecha_nacimiento DATE,
    CONSTRAINT uk_pacientes_documento UNIQUE (documento_identidad)
);

CREATE TABLE citas (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    medico_id UUID NOT NULL,
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    cancelada_en TIMESTAMP WITH TIME ZONE,
    clave_franja_medico VARCHAR(100),
    clave_franja_paciente VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_citas_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_citas_medico FOREIGN KEY (medico_id) REFERENCES medicos(id),
    CONSTRAINT uk_citas_franja_medico UNIQUE (clave_franja_medico),
    CONSTRAINT uk_citas_franja_paciente UNIQUE (clave_franja_paciente),
    CONSTRAINT ck_citas_estado CHECK (estado IN ('PROGRAMADA', 'CANCELADA', 'ATENDIDA'))
);

CREATE TABLE penalizaciones (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    cita_id UUID NOT NULL,
    registrada_en TIMESTAMP WITH TIME ZONE NOT NULL,
    motivo VARCHAR(200) NOT NULL,
    CONSTRAINT fk_penalizaciones_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes(id),
    CONSTRAINT fk_penalizaciones_cita FOREIGN KEY (cita_id) REFERENCES citas(id),
    CONSTRAINT uk_penalizaciones_cita UNIQUE (cita_id)
);

CREATE INDEX idx_citas_medico_fecha ON citas (medico_id, fecha_hora);
CREATE INDEX idx_citas_paciente_fecha ON citas (paciente_id, fecha_hora);
CREATE INDEX idx_citas_estado_fecha ON citas (estado, fecha_hora);
CREATE INDEX idx_penalizaciones_paciente_fecha ON penalizaciones (paciente_id, registrada_en);
