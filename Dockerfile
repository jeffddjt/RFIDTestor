FROM microsoft/dotnet
EXPOSE 5000
RUN mkdir /app
WORKDIR /app
COPY . .
CMD dotnet run

