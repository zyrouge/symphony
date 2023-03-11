export const s = {
    let: <U, V>(value: U, transform: (value: U) => V): V => transform(value),
};
